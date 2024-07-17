package aws.vpc.elasticcache;

import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.constructs.Construct;

public class RedisSecurityGroup {

    private final Construct scope;

    public RedisSecurityGroup(Construct scope) {
        this.scope = scope;
    }

    public SecurityGroup createRedisSecurityGroup(String redisId, IVpc vpc) {
        SecurityGroup redisSecurityGroup = SecurityGroup.Builder.create(scope, redisId + "-SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .description("Security group for Redis cluster " + redisId)
                .build();

        TagUtils.applyTags(redisSecurityGroup);

        redisSecurityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(6379),
                "Allow Redis access"
        );

        return redisSecurityGroup;
    }
}
