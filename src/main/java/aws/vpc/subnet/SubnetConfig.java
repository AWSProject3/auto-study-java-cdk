package aws.vpc.subnet;

import static aws.vpc.subnet.type.AZType.AZ_1A;
import static aws.vpc.subnet.type.AZType.AZ_1B;
import static aws.vpc.subnet.type.SubnetType.PRIVATE_TYPE;
import static aws.vpc.subnet.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.type.AZType;
import aws.vpc.util.ScopeValidator;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.Vpc;

public class SubnetConfig {
    private final Construct scope;
    private final Vpc vpc;

    public SubnetConfig(Construct scope) {
        this.scope = scope;
        this.vpc = ScopeValidator.extractVpcBy(scope);
    }

    public List<SubnetDto> configure() {
        CfnSubnet publicSubnet1 = createPublicSubnet("PublicSubnet1", "20.0.1.0/24", AZ_1A);
        CfnSubnet publicSubnet2 = createPublicSubnet("PublicSubnet2", "20.0.2.0/24", AZ_1B);
        CfnSubnet privateSubnet1 = createPrivateSubnet("PrivateSubnet1", "20.0.3.0/24", AZ_1A);
        CfnSubnet privateSubnet2 = createPrivateSubnet("PrivateSubnet2", "20.0.4.0/24", AZ_1B);

        List<SubnetDto> subnetDtos = new ArrayList<>();
        subnetDtos.add(new SubnetDto(PUBLIC_TYPE, publicSubnet1.getAttrSubnetId(), AZ_1A));
        subnetDtos.add(new SubnetDto(PUBLIC_TYPE, publicSubnet2.getAttrSubnetId(), AZ_1B));
        subnetDtos.add(new SubnetDto(PRIVATE_TYPE, privateSubnet1.getAttrSubnetId(), AZ_1A));
        subnetDtos.add(new SubnetDto(PRIVATE_TYPE, privateSubnet2.getAttrSubnetId(), AZ_1B));
        return subnetDtos;
    }

    private CfnSubnet createPublicSubnet(String subnetId, String cidr, AZType az) {
        return CfnSubnet.Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(String.valueOf(az))
                .mapPublicIpOnLaunch(true)
                .build();
    }

    private CfnSubnet createPrivateSubnet(String subnetId, String cidr, AZType az) {
        return CfnSubnet.Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(String.valueOf(az))
                .build();
    }
}
