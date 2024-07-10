package aws.eks;

import java.util.Arrays;
import java.util.List;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.eks.AutoScalingGroupCapacityOptions;
import software.amazon.awscdk.services.eks.CapacityType;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.CfnAddonProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.ClusterProps;
import software.amazon.awscdk.services.eks.KubernetesVersion;
import software.amazon.awscdk.services.eks.NodegroupAmiType;
import software.amazon.awscdk.services.eks.NodegroupOptions;
import software.amazon.awscdk.services.iam.AccountRootPrincipal;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

public class EksConfig {
    private static final String DEFAULT_VPC_ID = "auto-study-vpc";

    private final Construct scope;

    public EksConfig(Construct scope) {
        this.scope = scope;
    }

    public void configure(String clusterName) {
        IVpc vpc = lookupExistingVpc();
        Cluster cluster = createCluster(clusterName, vpc);
        configureNodeGroup(cluster);
        configureAddons(cluster);
    }

    private IVpc lookupExistingVpc() {
        return Vpc.fromLookup(scope, DEFAULT_VPC_ID, VpcLookupOptions.builder().build());
    }

    private Cluster createCluster(String clusterName, IVpc vpc) {
        Role masterRole = createMasterRole();

        return new Cluster(scope, "EksCluster", ClusterProps.builder()
                .version(KubernetesVersion.V1_21)
                .clusterName(clusterName)
                .mastersRole(masterRole)
                .vpc(vpc)
                .vpcSubnets(List.of(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build()))
                .defaultCapacity(0)
                .build());
    }

    private Role createMasterRole() {
        return Role.Builder.create(scope, "MasterRole")
                .assumedBy(new AccountRootPrincipal())
                .build();
    }

    private void configureNodeGroup(Cluster cluster) {
        cluster.addNodegroupCapacity("OnDemandNodes", NodegroupOptions.builder()
                .instanceTypes(List.of(new InstanceType("m5.xlarge")))
                .minSize(3)
                .maxSize(6)
                .desiredSize(3)
                .amiType(NodegroupAmiType.AL2_X86_64)
                .capacityType(CapacityType.ON_DEMAND)
                .build());
    }

    private void configureAddons(Cluster cluster) {
        for (CfnAddon addOn : createAddons(cluster.getClusterName())) {
            cluster.addAutoScalingGroupCapacity(addOn.getNode().getId(),
                    AutoScalingGroupCapacityOptions.builder().build());
        }
    }

    private List<CfnAddon> createAddons(String clusterName) {
        CfnAddonProps props = createAddonProps(clusterName);
        List<CfnAddon> addOns = Arrays.asList(
                new CfnAddon(scope, "VpcCniAddon", props),
                new CfnAddon(scope, "CoreDnsAddon", props),
                new CfnAddon(scope, "KubeProxyAddon", props),
                new CfnAddon(scope, "AwsLoadBalancerControllerAddon", props),
                new CfnAddon(scope, "ArgoCdAddon", props)
        );
        return addOns;
    }

    private CfnAddonProps createAddonProps(String clusterName) {
        return CfnAddonProps.builder().clusterName(clusterName).build();
    }
}
