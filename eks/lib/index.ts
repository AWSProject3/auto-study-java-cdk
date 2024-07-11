import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as blueprints from '@aws-quickstart/eks-blueprints';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as cr from 'aws-cdk-lib/custom-resources';
import {Construct} from 'constructs';

export class EksConfigStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpcName = 'auto-study-vpc';

        const lambdaLayer = new lambda.LayerVersion(this, 'VpcInfoLayer', {
            code: lambda.Code.fromAsset('lambda', {
                bundling: {
                    image: lambda.Runtime.PYTHON_3_12.bundlingImage,
                    command: [
                        'bash', '-c',
                        'pip install -r requirements.txt -t /asset-output && cp -au . /asset-output'
                    ],
                },
            }),
            compatibleRuntimes: [lambda.Runtime.PYTHON_3_12],
            description: 'A layer to add boto3',
        });

        const getVpcInfoFunction = new lambda.Function(this, 'GetVpcInfoFunction', {
            runtime: lambda.Runtime.PYTHON_3_12,
            handler: 'getVpcInfo.handler',
            code: lambda.Code.fromAsset('lambda'),
            layers: [lambdaLayer],
            role: new iam.Role(this, 'LambdaExecutionRole', {
                assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
                managedPolicies: [
                    iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'),
                ],
            }),
        });

        const vpcInfoProvider = new cr.Provider(this, 'VpcInfoProvider', {
            onEventHandler: getVpcInfoFunction,
        });

        const vpcInfoResource = new cdk.CustomResource(this, 'VpcInfoResource', {
            serviceToken: vpcInfoProvider.serviceToken,
            properties: {
                VpcName: vpcName,
            },
        });

        const vpcId = vpcInfoResource.getAttString('VpcId');
        const publicSubnetIds = vpcInfoResource.getAtt('PublicSubnetIds').toString().split(',');
        const privateSubnetIds = vpcInfoResource.getAtt('PrivateSubnetIds').toString().split(',');

        const availabilityZones = ['us-east-2a', 'us-east-2b'];
        console.log(`Public Subnets: ${publicSubnetIds}`);
        console.log(`Private Subnets: ${privateSubnetIds}`);

        const vpc = ec2.Vpc.fromVpcAttributes(this, 'ExistingVpc', {
            vpcId: vpcId,
            availabilityZones: ['us-east-2a', 'us-east-2b'],
            publicSubnetIds: publicSubnetIds,
            privateSubnetIds: privateSubnetIds,
        });

        const privateSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS});

        const clusterProvider = new blueprints.GenericClusterProvider({
            version: cdk.aws_eks.KubernetesVersion.V1_27,
            mastersRole: this.createMasterRole(),
            managedNodeGroups: [
                {
                    id: "OnDemandNodes",
                    instanceTypes: [new ec2.InstanceType('m5.xlarge')],
                    minSize: 3,
                    maxSize: 6,
                    desiredSize: 3,
                    nodeGroupCapacityType: cdk.aws_eks.CapacityType.ON_DEMAND,
                    nodeGroupSubnets: privateSubnets,
                },
            ],
            vpcSubnets: [privateSubnets]
        });

        const blueprint = blueprints.EksBlueprint.builder()
            .account(this.account)
            .region(this.region)
            .addOns(
                new blueprints.addons.VpcCniAddOn(),
                new blueprints.addons.CoreDnsAddOn(),
                new blueprints.addons.KubeProxyAddOn(),
                new blueprints.addons.AwsLoadBalancerControllerAddOn(),
                new blueprints.addons.ArgoCDAddOn()
            )
            .clusterProvider(clusterProvider)
            .resourceProvider(blueprints.GlobalResources.Vpc, new blueprints.VpcProvider(vpc.vpcId))
            .build(this, 'auto-study-eks');

        this.tagSubnets(vpc, 'auto-study-eks');
    }

    private createMasterRole(): iam.Role {
        return new iam.Role(this, 'MasterRole', {
            assumedBy: new iam.AccountRootPrincipal(),
        });
    }

    private tagSubnets(vpc: ec2.IVpc, clusterName: string) {
        vpc.publicSubnets.forEach(subnet => {
            cdk.Tags.of(subnet).add('kubernetes.io/role/elb', '1');
        });

        vpc.privateSubnets.forEach(subnet => {
            cdk.Tags.of(subnet).add('kubernetes.io/role/internal-elb', '1');
            cdk.Tags.of(subnet).add(`kubernetes.io/cluster/${clusterName}`, 'shared');
        });
    }
}

const app = new cdk.App();
new EksConfigStack(app, 'EksConfigStack', {
    env: {
        account: process.env.CDK_DEFAULT_ACCOUNT,
        region: process.env.CDK_DEFAULT_REGION
    }
});
app.synth();
