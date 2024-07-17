package aws.vpc.elasticcache;

import aws.vpc.VpcInfraManager;
import aws.vpc.util.TagUtils;
import java.util.Map;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.elasticache.CfnParameterGroup;
import software.amazon.awscdk.services.elasticache.CfnReplicationGroup;
import software.amazon.awscdk.services.elasticache.CfnSubnetGroup;
import software.constructs.Construct;

public class RedisConfigurator {
    private final Construct scope;
    private final VpcInfraManager vpcInfraManager;
    private final RedisSubnetGroup redisSubnetGroup;
    private final RedisSecurityGroup redisSecurityGroup;

    public RedisConfigurator(Construct scope, VpcInfraManager vpcInfraManager, RedisSubnetGroup redisSubnetGroup,
                             RedisSecurityGroup redisSecurityGroup) {
        this.scope = scope;
        this.vpcInfraManager = vpcInfraManager;
        this.redisSubnetGroup = redisSubnetGroup;
        this.redisSecurityGroup = redisSecurityGroup;
    }

    public void configure(String redisId, String clusterName, int numCacheClusters, String engineVersion) {
        IVpc vpc = vpcInfraManager.findExistingVpc(scope);
        createRedisCluster(redisId, clusterName, numCacheClusters, engineVersion, vpc);
    }

    private void createRedisCluster(String redisId, String clusterName, int numCacheClusters, String engineVersion,
                                    IVpc vpc) {
        CfnSubnetGroup subnetGroup = redisSubnetGroup.createRedisSubnetGroup(redisId, vpc);
        SecurityGroup securityGroup = redisSecurityGroup.createRedisSecurityGroup(redisId, vpc);
        CfnParameterGroup parameterGroup = createParameterGroup(redisId);

        CfnReplicationGroup redisCluster = CfnReplicationGroup.Builder.create(scope, redisId)
                .replicationGroupId(clusterName)
                .replicationGroupDescription("Redis cluster for " + clusterName)
                .engine("redis")
                .cacheNodeType("cache.t3.small")
                .numCacheClusters(numCacheClusters)
                .securityGroupIds(java.util.List.of(securityGroup.getSecurityGroupId()))
                .cacheSubnetGroupName(subnetGroup.getRef())
                .engineVersion(engineVersion)
                .cacheParameterGroupName(parameterGroup.getRef())
                .automaticFailoverEnabled(true)
                .multiAzEnabled(true)
                .build();

        TagUtils.applyTags(redisCluster);
    }

    private CfnParameterGroup createParameterGroup(String redisId) {
        return CfnParameterGroup.Builder.create(scope, redisId + "-ParamGroup")
                .cacheParameterGroupFamily("redis6.x")
                .description("Parameter group for ElastiCache Redis")
                .properties(Map.of(
                        "maxmemory-policy", "allkeys-lru",
                        "timeout", "300",
                        "notify-keyspace-events", "KEA"
                ))
                .build();
    }
}
