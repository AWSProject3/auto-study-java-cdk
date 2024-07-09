package aws.vpc;

import aws.vpc.util.ScopeValidator;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnInternetGateway;
import software.amazon.awscdk.services.ec2.CfnVPCGatewayAttachment;
import software.amazon.awscdk.services.ec2.Vpc;

public class IgwConfig {
    private final String SUFFIX = "attachment";

    private final Construct scope;
    private final Vpc vpc;

    public IgwConfig(Construct scope) {
        this.scope = scope;
        this.vpc = ScopeValidator.extractVpcBy(scope);
    }

    public void configure(String igwId) {
        CfnInternetGateway igw = createIgw(igwId);
        CfnVPCGatewayAttachment.Builder.create(scope, igwId + SUFFIX)
                .vpcId(vpc.getVpcId())
                .internetGatewayId(igw.getAttrInternetGatewayId())
                .build();
    }

    private CfnInternetGateway createIgw(String igwId) {
        return CfnInternetGateway.Builder.create(scope, igwId).build();
    }
}
