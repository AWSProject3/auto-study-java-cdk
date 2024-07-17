package aws.vpc.elasticcache;

import aws.vpc.VpcInfraManager;
import aws.vpc.util.TagUtils;
import java.util.List;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.elasticache.CfnSubnetGroup;
import software.constructs.Construct;

public class RedisSubnetGroup {

    private final VpcInfraManager vpcInfraManager;
    private final Construct scope;

    public RedisSubnetGroup(VpcInfraManager vpcInfraManager, Construct scope) {
        this.vpcInfraManager = vpcInfraManager;
        this.scope = scope;
    }

    public CfnSubnetGroup createRedisSubnetGroup(String redisId, IVpc vpc) {
        List<String> subnetIds = vpc.getPrivateSubnets().stream()
                .map(ISubnet::getSubnetId)
                .toList();

        CfnSubnetGroup subnetGroup = CfnSubnetGroup.Builder.create(scope, redisId + "-SubnetGroup")
                .description("Subnet group for " + redisId)
                .subnetIds(subnetIds)
                .build();

        TagUtils.applyTags(subnetGroup);
        return subnetGroup;
    }
}
