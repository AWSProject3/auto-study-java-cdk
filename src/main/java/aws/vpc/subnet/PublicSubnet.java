package aws.vpc.subnet;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.type.AZType;
import aws.vpc.subnet.type.SubnetType;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.Vpc;

public class PublicSubnet {

    private final Construct scope;
    private final Vpc vpc;

    public PublicSubnet(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public SubnetDto configSubnet(String subnetId, String cidr, AZType az) {
        CfnSubnet publicSubnet = createPublicSubnet(subnetId, cidr, az);
        return new SubnetDto(SubnetType.PUBLIC_TYPE, publicSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPublicSubnet(String subnetId, String cidr, AZType az) {
        return CfnSubnet.Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(String.valueOf(az))
                .mapPublicIpOnLaunch(true)
                .build();
    }
}
