package aws.vpc.rds;

import java.util.Collections;
import software.amazon.awscdk.services.ec2.IPeer;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroup.Builder;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
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

        rdsSecurityGroup.addIngressRule(
                createEksSecurityGroup(),
                Port.tcp(3306),
                "Allow MySQL access from EKS pods"
        );

        return rdsSecurityGroup;
    }

    private IPeer createEksSecurityGroup() {
        SecurityGroup securityGroup = Builder.create(scope, "EksSecurityGroup")
                .vpc(getVpc())
                .allowAllOutbound(true)
                .description("Security group for EKS cluster")
                .build();

        securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.allTraffic(),
                "Allow all inbound traffic"

        );
        return securityGroup;
    }

    private IVpc getVpc() {
        return Vpc.fromLookup(scope, "ExistingVPC", VpcLookupOptions.builder()
                .tags(Collections.singletonMap("Name", "auto-study"))
                .build());
    }
}
