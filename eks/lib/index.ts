import * as cdk from 'aws-cdk-lib';
import {custom_resources} from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {IVpc, SelectedSubnets} from 'aws-cdk-lib/aws-ec2';
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

        const publicSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PUBLIC});
        const privateSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS});

        this.tagToPublicSubnets(publicSubnets, vpc);
        this.tagToPrivateSubnets(privateSubnets, vpc);

        const blueprint = this.configureEks(publicSubnets, privateSubnets, vpc);
    }

    private tagToPublicSubnets(publicSubnets: SelectedSubnets, vpc: IVpc) {
        const tagPublicSubnets = new custom_resources.AwsCustomResource(this, 'TagPublicSubnets', {
            onCreate: {
                service: 'EC2',
                action: 'createTags',
                parameters: {
                    Resources: publicSubnets.subnetIds,
                    Tags: [
                        {Key: 'kubernetes.io/role/elb', Value: '1'}
                    ]
                },
                physicalResourceId: custom_resources.PhysicalResourceId.of('PublicSubnetTagging')
            },
            policy: custom_resources.AwsCustomResourcePolicy.fromSdkCalls({resources: custom_resources.AwsCustomResourcePolicy.ANY_RESOURCE})
        });

        tagPublicSubnets.node.addDependency(vpc);
    }

    private tagToPrivateSubnets(privateSubnets: SelectedSubnets, vpc: IVpc) {
        const tagPrivateSubnets = new custom_resources.AwsCustomResource(this, 'TagPrivateSubnets', {
            onCreate: {
                service: 'EC2',
                action: 'createTags',
                parameters: {
                    Resources: privateSubnets.subnetIds,
                    Tags: [
                        {Key: 'kubernetes.io/role/internal-elb', Value: '1'},
                        {Key: `kubernetes.io/cluster/auto-study-eks`, Value: 'shared'}
                    ]
                },
                physicalResourceId: custom_resources.PhysicalResourceId.of('PrivateSubnetTagging')
            },
            policy: custom_resources.AwsCustomResourcePolicy.fromSdkCalls({resources: custom_resources.AwsCustomResourcePolicy.ANY_RESOURCE})
        });

        tagPrivateSubnets.node.addDependency(vpc);
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

    private createClusterProvider(publicSubnets: SelectedSubnets, privateSubnets: SelectedSubnets) {
        return new blueprints.GenericClusterProvider({
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
    }

    private configureEks(publicSubnets: SelectedSubnets, privateSubnets: SelectedSubnets, vpc: IVpc) {
        return blueprints.EksBlueprint.builder()
            .account(this.account)
            .region(this.region)
            .addOns(
                new blueprints.addons.VpcCniAddOn(),
                new blueprints.addons.CoreDnsAddOn(),
                new blueprints.addons.KubeProxyAddOn(),
                new blueprints.addons.AwsLoadBalancerControllerAddOn(),
                new blueprints.addons.EbsCsiDriverAddOn(),
                // new blueprints.addons.ArgoCDAddOn(),
                new blueprints.ExternalsSecretsAddOn({
                    namespace: 'app',
                    iamPolicies: [this.createExternalSecretsPolicy()]
                })
            )
            .clusterProvider(this.createClusterProvider(publicSubnets, privateSubnets))
            .resourceProvider(blueprints.GlobalResources.Vpc, new blueprints.DirectVpcProvider(vpc))
            .version(eks.KubernetesVersion.V1_30)
            .build(this, 'auto-study-eks');
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
