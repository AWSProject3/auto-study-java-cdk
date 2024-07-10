package aws.vpc.subnet;

import static aws.vpc.subnet.type.AzType.AZ_1A;
import static aws.vpc.subnet.type.AzType.AZ_1B;

import aws.vpc.subnet.dto.SubnetDto;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class SubnetConfig {
    private final Construct scope;
    private final Vpc vpc;

    public SubnetConfig(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public List<SubnetDto> configure() {
        List<SubnetDto> subnetDtos = new ArrayList<>();

        PublicSubnet publicSubnet1 = new PublicSubnet(scope, vpc);
        subnetDtos.add(publicSubnet1.configSubnet("PublicSubnet1", "20.0.10.0/24", AZ_1A));

        PublicSubnet publicSubnet2 = new PublicSubnet(scope, vpc);
        subnetDtos.add(publicSubnet2.configSubnet("PublicSubnet2", "20.0.20.0/24", AZ_1B));

        PrivateSubnet privateSubnet1 = new PrivateSubnet(scope, vpc);
        subnetDtos.add(privateSubnet1.configSubnet("PrivateSubnet1", "20.0.30.0/24", AZ_1A));

        PrivateSubnet privateSubnet2 = new PrivateSubnet(scope, vpc);
        subnetDtos.add(privateSubnet2.configSubnet("PrivateSubnet2", "20.0.40.0/24", AZ_1B));

        return subnetDtos;
    }
}
