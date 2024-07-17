import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {SelectedSubnets} from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as eks from 'aws-cdk-lib/aws-eks';
import * as blueprints from '@aws-quickstart/eks-blueprints';
import {Construct} from 'constructs';

export class EksConfigurator extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpc = ec2.Vpc.fromLookup(this, 'ImportedVpc', {
            tags: {'Name': 'auto-study'}
        });

        const publicSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_ISOLATED});
        const privateSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS});

        const clusterProvider = new blueprints.GenericClusterProvider({
            version: eks.KubernetesVersion.V1_30,
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
                    nodeGroupCapacityType: eks.CapacityType.ON_DEMAND,
                    nodeGroupSubnets: {subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS},
                    nodeRole: blueprints.getResource(context => {
                        const nodeGroupRole = new iam.Role(context.scope, 'EksNodeGroupRole', {
                            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
                        });

                        nodeGroupRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEKSWorkerNodePolicy'));
                        nodeGroupRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEKS_CNI_Policy'));
                        nodeGroupRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryReadOnly'));

                        return nodeGroupRole;
                    }),
                },
            ],
            vpcSubnets: [publicSubnets, privateSubnets]
        });

        const blueprint = blueprints.EksBlueprint.builder()
            .account(this.account)
            .region(this.region)
            .addOns(
                new blueprints.addons.VpcCniAddOn(),
                new blueprints.addons.CoreDnsAddOn(),
                new blueprints.addons.KubeProxyAddOn(),
                new blueprints.addons.AwsLoadBalancerControllerAddOn(),
                new blueprints.addons.EbsCsiDriverAddOn(),
                new blueprints.addons.ArgoCDAddOn(),
                new blueprints.ExternalsSecretsAddOn({
                    namespace: 'app',
                    iamPolicies: [this.createExternalSecretsPolicy()]
                })
            )
            .clusterProvider(clusterProvider)
            .resourceProvider(blueprints.GlobalResources.Vpc, new blueprints.DirectVpcProvider(vpc))
            .version(eks.KubernetesVersion.V1_30)
            .build(this, 'auto-study-eks');

        this.tagSubnets(publicSubnets, privateSubnets, 'auto-study-eks');
    }

    private createExternalSecretsPolicy(): iam.PolicyStatement {
        return new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            actions: [
                'secretsmanager:GetResourcePolicy',
                'secretsmanager:GetSecretValue',
                'secretsmanager:DescribeSecret',
                'secretsmanager:ListSecretVersionIds'
            ],
            resources: [`arn:aws:secretsmanager:${this.region}:${this.account}:secret:*`],
        });
    }

    private tagSubnets(publicSubnets: SelectedSubnets, privateSubnets: SelectedSubnets, clusterName: string) {
        publicSubnets.subnets.forEach(subnet => {
            cdk.Tags.of(subnet).add('kubernetes.io/role/elb', '1');
        });

        privateSubnets.subnets.forEach(subnet => {
            cdk.Tags.of(subnet).add('kubernetes.io/role/internal-elb', '1');
            cdk.Tags.of(subnet).add(`kubernetes.io/cluster/${clusterName}`, 'shared');
        });
    }
}

const app = new cdk.App();
new EksConfigurator(app, 'EksStack', {
    env: {
        account: process.env.CDK_DEFAULT_ACCOUNT,
        region: process.env.CDK_DEFAULT_REGION
    }
});
app.synth();
