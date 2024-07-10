package aws.vpc.eks;

import aws.vpc.common.VpcInfraManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceType;
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
    private final Construct scope;
    private final VpcInfraManager vpcInfraManager;

    public EksConfig(Construct scope, VpcInfraManager vpcInfraManager) {
        this.scope = scope;
        this.vpcInfraManager = vpcInfraManager;
    }

    public void configure(String clusterName) {
        IVpc vpc = vpcInfraManager.findExistingVpc(scope);
        Cluster cluster = createCluster(clusterName, vpc);
        tagSubnetsForEks(clusterName);
        configureNodeGroup(cluster);
        configureAddons(cluster);
    }

    private Cluster createCluster(String clusterName, IVpc vpc) {
        Role masterRole = createMasterRole();

        return new Cluster(scope, "EksCluster", ClusterProps.builder()
                .version(KubernetesVersion.V1_25)
                .clusterName(clusterName)
                .mastersRole(masterRole)
                .vpc(vpc)
                .vpcSubnets(Collections.singletonList(vpcInfraManager.createPrivateSubnetSelector(scope)))
                .defaultCapacity(0)
//                .kubectlLayer(new KubectlLayer(scope, "KubectlLayer"))
                .build());
    }

    private void tagSubnetsForEks(String clusterName) {
        tagPublicSubnets();
        tagPrivateSubnets(clusterName);
    }

    private void tagPublicSubnets() {
        Map<String, String> publicTags = new HashMap<>();
        publicTags.put("kubernetes.io/role/elb", "1");
        vpcInfraManager.tagPublicSubnets(publicTags, scope);
    }

    private void tagPrivateSubnets(String clusterName) {
        Map<String, String> privateTags = new HashMap<>();
        privateTags.put("kubernetes.io/role/internal-elb", "1");
        privateTags.put("kubernetes.io/cluster/" + clusterName, "shared");
        vpcInfraManager.tagPrivateSubnets(privateTags, scope);
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
                    AutoScalingGroupCapacityOptions
                            .builder()
                            .instanceType(new InstanceType("m3.xlarge"))
                            .build()
            );
        }
    }

    private List<CfnAddon> createAddons(String clusterName) {
        return List.of(
                new CfnAddon(scope, "VpcCniAddon", createAddonProps(clusterName, "vpc-cni")),
                new CfnAddon(scope, "CoreDnsAddon", createAddonProps(clusterName, "coredns")),
                new CfnAddon(scope, "KubeProxyAddon", createAddonProps(clusterName, "kube-proxy"))
//                new CfnAddon(scope, "AwsLoadBalancerControllerAddon",
//                        createAddonProps(clusterName, "aws-load-balancer-controller")),
//                new CfnAddon(scope, "ArgoCdAddon", createAddonProps(clusterName, "ArgoCd"))
        );
    }

    private CfnAddonProps createAddonProps(String clusterName, String addonName) {
        return CfnAddonProps.builder().clusterName(clusterName).addonName(addonName).build();
    }
}
