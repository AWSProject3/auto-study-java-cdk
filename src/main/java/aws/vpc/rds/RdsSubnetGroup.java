package aws.vpc.rds;

import aws.vpc.VpcInfraManager;
import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.amazon.awscdk.services.rds.SubnetGroup.Builder;
import software.constructs.Construct;

public class RdsSubnetGroup {

    private final VpcInfraManager vpcInfraManager;
    private final Construct scope;

    public RdsSubnetGroup(VpcInfraManager vpcInfraManager, Construct scope) {
        this.vpcInfraManager = vpcInfraManager;
        this.scope = scope;
    }

    public SubnetGroup createDbSubnetGroup(String rdsId, IVpc vpc) {
        SubnetGroup subnetGroup = Builder.create(scope, rdsId + "-SubnetGroup")
                .description("Subnet group for " + rdsId)
                .vpc(vpc)
                .vpcSubnets(createSubnetSelection())
                .build();

        TagUtils.applyTags(subnetGroup);
        return subnetGroup;
    }

    private SubnetSelection createSubnetSelection() {
        return vpcInfraManager.createPrivateSubnetSelector(scope);
    }
}
