package aws.vpc;

import java.util.Collections;
import java.util.Map;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcConfigurator {
    private final Construct scope;

    public VpcConfigurator(Construct scope) {
        this.scope = scope;
    }

    public Vpc configureEmptyVpc(String vpcId, Map<String, String> tags) {
        Vpc vpc = Vpc.Builder.create(scope, vpcId)
                .ipAddresses(IpAddresses.cidr("20.0.0.0/16"))
                .maxAzs(2)
                .subnetConfiguration(Collections.emptyList())
                .build();

        applyTags(vpc, tags);

        return vpc;
    }

    private void applyTags(Vpc vpc, Map<String, String> tags) {
        tags.forEach((key, value) -> Tags.of(vpc).add(key, value));
    }
}
