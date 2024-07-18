package aws.vpc.rds;

import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.constructs.Construct;

public class RdsSecurityGroup {

    private final Construct scope;

    public RdsSecurityGroup(Construct scope) {
        this.scope = scope;
    }

    public SecurityGroup createRdsSecurityGroup(String rdsId, IVpc vpc) {
        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(scope, rdsId + "-SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .description("Security group for RDS instance " + rdsId)
                .build();

        TagUtils.applyTags(rdsSecurityGroup);

        rdsSecurityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(3306),
                "Allow MySQL access from EKS pods"
        );

        return rdsSecurityGroup;
    }
}
