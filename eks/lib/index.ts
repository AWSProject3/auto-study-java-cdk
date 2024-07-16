import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as eks from 'aws-cdk-lib/aws-eks';
import * as blueprints from '@aws-quickstart/eks-blueprints';
import {ClusterInfo} from '@aws-quickstart/eks-blueprints';
import {Construct} from 'constructs';
import {AwsCustomResource, AwsCustomResourcePolicy, PhysicalResourceId} from 'aws-cdk-lib/custom-resources';

export class EksConfigStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpc = ec2.Vpc.fromLookup(this, 'ImportedVpc', {
            tags: {'Name': 'auto-study'}
        });

        const privateSubnets = vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS});

        const clusterName = 'auto-study-eks';

        const existingCluster = this.checkExistingCluster(clusterName);

        if (existingCluster.getResponseField('cluster.name')) {
            this.updateExistingCluster(clusterName, vpc, privateSubnets);
        } else {
            this.createNewCluster(vpc, privateSubnets, clusterName);
        }

        this.tagSubnets(vpc, clusterName);
    }

    private checkExistingCluster(clusterName: string): AwsCustomResource {
        const checkClusterRole = new iam.Role(this, 'CheckClusterRole', {
            assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
        });

        checkClusterRole.addToPolicy(new iam.PolicyStatement({
            actions: ['eks:DescribeCluster'],
            resources: [`arn:aws:eks:${this.region}:${this.account}:cluster/${clusterName}`],
        }));

        checkClusterRole.addToPolicy(new iam.PolicyStatement({
            actions: ['sts:AssumeRole'],
            resources: [`arn:aws:iam::${this.account}:role/EksConfigStackautostudyeks0876BD-MasterRole7C9FAFA5-TTEH0xq5Amfq`],
        }));

        return new AwsCustomResource(this, 'CheckExistingCluster', {
            onCreate: {
                service: 'EKS',
                action: 'describeCluster',
                parameters: {
                    name: clusterName
                },
                physicalResourceId: PhysicalResourceId.of(clusterName),
            },
            onUpdate: {
                service: 'EKS',
                action: 'describeCluster',
                parameters: {
                    name: clusterName
                },
                physicalResourceId: PhysicalResourceId.of(clusterName),
            },
            policy: AwsCustomResourcePolicy.fromStatements([
                new iam.PolicyStatement({
                    actions: ['eks:DescribeCluster'],
                    resources: [`arn:aws:eks:${this.region}:${this.account}:cluster/${clusterName}`],
                }),
            ]),
        });
    }

    private updateExistingCluster(clusterName: string, vpc: ec2.IVpc, privateSubnets: ec2.SelectedSubnets) {
        const existingOidcProvider = iam.OpenIdConnectProvider.fromOpenIdConnectProviderArn(
            this,
            'ExistingOidcProvider',
            `arn:aws:iam::${this.account}:oidc-provider/oidc.eks.${this.region}.amazonaws.com/id/F54C1B0B8C9CC2613FA2EF6B4DCF7FAF`
        );

        // 수정: 마스터 역할의 신뢰 관계 수정
        const clusterCreatorRole = new iam.Role(this, 'MasterRole', {
            assumedBy: new iam.CompositePrincipal(
                new iam.ServicePrincipal('eks.amazonaws.com'),
                new iam.ServicePrincipal('lambda.amazonaws.com')
            ),
            roleName: 'EksConfigStackautostudyeks0876BD-MasterRole7C9FAFA5-TTEH0xq5Amfq'
        });

        const existingCluster = eks.Cluster.fromClusterAttributes(this, 'ImportedCluster', {
            clusterName: clusterName,
            vpc: vpc,
            openIdConnectProvider: existingOidcProvider,
            kubectlRoleArn: clusterCreatorRole.roleArn
        });

        const nodeRole = this.createNodeRole();

        new eks.Nodegroup(this, 'UpdatedNodeGroup', {
            cluster: existingCluster,
            instanceTypes: [new ec2.InstanceType('m5.xlarge')],
            minSize: 3,
            maxSize: 6,
            desiredSize: 3,
            subnets: privateSubnets,
            nodeRole: nodeRole,
        });

        const ebsCsiDriverAddOn = new blueprints.addons.EbsCsiDriverAddOn();
        const clusterInfo = {
            cluster: existingCluster,
            version: eks.KubernetesVersion.V1_27,
        } as ClusterInfo;
        ebsCsiDriverAddOn.deploy(clusterInfo);
    }

    private createNewCluster(vpc: ec2.IVpc, privateSubnets: ec2.SelectedSubnets, clusterName: string) {
        const nodeRole = this.createNodeRole();

        const clusterProvider = new blueprints.GenericClusterProvider({
            version: eks.KubernetesVersion.V1_27,
            mastersRole: blueprints.getResource(context => {
                return new iam.Role(context.scope, 'MasterRole', {
                    assumedBy: new iam.CompositePrincipal(
                        new iam.AccountRootPrincipal(),
                        new iam.ServicePrincipal('lambda.amazonaws.com')
                    )
                });
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
                    nodeRole: nodeRole,
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
                new blueprints.addons.ArgoCDAddOn(),
                new blueprints.addons.ExternalsSecretsAddOn({
                    namespace: 'app'
                }),
                new blueprints.addons.EbsCsiDriverAddOn()
            )
            .clusterProvider(clusterProvider)
            .resourceProvider(blueprints.GlobalResources.Vpc, new blueprints.DirectVpcProvider(vpc))
            .build(this, clusterName);
    }

    private createNodeRole(): iam.Role {
        const nodeRole = new iam.Role(this, 'EksNodeGroupRole', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
        });

        nodeRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEKSWorkerNodePolicy'));
        nodeRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEKS_CNI_Policy'));
        nodeRole.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryReadOnly'));

        nodeRole.addToPolicy(new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            actions: [
                'ec2:CreateSnapshot',
                'ec2:AttachVolume',
                'ec2:DetachVolume',
                'ec2:ModifyVolume',
                'ec2:DescribeAvailabilityZones',
                'ec2:DescribeInstances',
                'ec2:DescribeSnapshots',
                'ec2:DescribeTags',
                'ec2:DescribeVolumes',
                'ec2:DescribeVolumesModifications'
            ],
            resources: ['*'],
        }));

        return nodeRole;
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
