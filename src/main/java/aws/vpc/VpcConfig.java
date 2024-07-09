package aws.vpc;

import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcConfig {
    private final Construct scope;

    public VpcConfig(Construct scope) {
        this.scope = scope;
    }

    public Vpc configure(String vpcId) {
        return Vpc.Builder.create(scope, vpcId)
                .cidr("20.0.0.0/16")
                .maxAzs(2)
                .build();
    }
}
