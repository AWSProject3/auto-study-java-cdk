package aws.vpc;

import java.util.Collections;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.Vpc.Builder;
import software.constructs.Construct;

public class VpcConfigurator {
    private final Construct scope;

    public VpcConfigurator(Construct scope) {
        this.scope = scope;
    }

    public Vpc configureEmptyVpc(String vpcId) {
        Vpc vpc = Builder.create(scope, vpcId)
                .ipAddresses(IpAddresses.cidr("20.0.0.0/16"))
                .maxAzs(2)
                .subnetConfiguration(Collections.emptyList())
                .build();

        new CfnOutput(scope, "VpcId", CfnOutputProps.builder()
                .value(vpc.getVpcId())
                .exportName("auto-study-vpc-id")
                .build());

        return vpc;
    }
}
