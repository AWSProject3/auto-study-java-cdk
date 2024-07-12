import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as blueprints from '@aws-quickstart/eks-blueprints';
import {Construct} from 'constructs';

interface VpcInfo {
    vpcId: string;
    publicSubnetIds: string[];
    privateSubnetIds: string[];
}

function getVpcInfo(): VpcInfo {
    const vpcInfoJson = process.env.VPC_INFO;
    if (!vpcInfoJson) {
        throw new Error('VPC information not found');
    }
    return JSON.parse(vpcInfoJson);
}

export class EksConfigStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpcInfo = getVpcInfo();

        const vpc = ec2.Vpc.fromVpcAttributes(this, 'ImportedVpc', {
            vpcId: vpcInfo.vpcId,
            availabilityZones: this.availabilityZones,
            publicSubnetIds: vpcInfo.publicSubnetIds,
            privateSubnetIds: vpcInfo.privateSubnetIds,
        });

        const privateSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS});

        const clusterProvider = new blueprints.GenericClusterProvider({
            version: cdk.aws_eks.KubernetesVersion.V1_27,
            mastersRole: blueprints.getResource(context => {
                return new iam.Role(context.scope, 'MasterRole', {assumedBy: new iam.AccountRootPrincipal()});
            }),
            managedNodeGroups: [
                {
                    id: "OnDemandNodes",
                    instanceTypes: [new ec2.InstanceType('m5.xlarge')],
                    minSize: 3,
                    maxSize: 6,
                    desiredSize: 3,
                    nodeGroupCapacityType: cdk.aws_eks.CapacityType.ON_DEMAND,
                    nodeGroupSubnets: {subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS},
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
            .resourceProvider(blueprints.GlobalResources.Vpc, new blueprints.DirectVpcProvider(vpc))
            .build(this, 'auto-study-eks');

        this.tagSubnets(vpc, 'auto-study-eks');
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
