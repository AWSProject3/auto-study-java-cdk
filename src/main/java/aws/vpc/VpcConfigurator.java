package aws.vpc;

import aws.vpc.util.TagUtils;
import java.util.Collections;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcConfigurator {
    private final Construct scope;

    public VpcConfigurator(Construct scope) {
        this.scope = scope;
    }

    public Vpc configureEmptyVpc(String vpcId) {
        Vpc vpc = Vpc.Builder.create(scope, vpcId)
                .ipAddresses(IpAddresses.cidr("20.0.0.0/16"))
                .maxAzs(2)
                .subnetConfiguration(Collections.emptyList())
                .build();
        TagUtils.applyTags(vpc);
        return vpc;
    }
}
