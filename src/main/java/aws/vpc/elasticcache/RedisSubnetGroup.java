package aws.vpc.elasticcache;

import aws.vpc.VpcInfraManager;
import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
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
        CfnSubnetGroup subnetGroup = CfnSubnetGroup.Builder.create(scope, redisId + "-SubnetGroup")
                .description("Subnet group for " + redisId)
                .subnetIds(vpc.selectSubnets(createSubnetSelection()).getSubnetIds())
                .build();

        TagUtils.applyTags(subnetGroup);
        return subnetGroup;
    }

    private SubnetSelection createSubnetSelection() {
        return vpcInfraManager.createPrivateSubnetSelector(scope);
    }
}
