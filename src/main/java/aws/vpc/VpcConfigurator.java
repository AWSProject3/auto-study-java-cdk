package aws.vpc;

import java.util.Collections;
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
        return Builder.create(scope, vpcId)
                .ipAddresses(IpAddresses.cidr("20.0.0.0/16"))
                .maxAzs(2)
                .subnetConfiguration(Collections.emptyList())
                .build();
    }
}
