package aws.vpc.subnet;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.type.AZType;
import aws.vpc.subnet.type.SubnetType;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.CfnSubnet.Builder;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class PrivateSubnet {

    private final Construct scope;
    private final Vpc vpc;

    public PrivateSubnet(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public SubnetDto configSubnet(String subnetId, String cidr, AZType az) {
        CfnSubnet privateSubnet = createPrivateSubnet(subnetId, cidr, az);
        return new SubnetDto(SubnetType.PRIVATE_TYPE, privateSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPrivateSubnet(String subnetId, String cidr, AZType az) {
        CfnSubnet subnet = createCfnSubnet(subnetId + az.getValue(), cidr, az);
        convertToISubnet(subnetId, subnet);
        return subnet;
    }

    private CfnSubnet createCfnSubnet(String subnetId, String cidr, AZType az) {
        return Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(az.getValue())
                .mapPublicIpOnLaunch(false)
                .build();
    }

    private void convertToISubnet(String subnetId, CfnSubnet subnet) {
        Subnet.fromSubnetId(scope, subnetId, subnet.getAttrSubnetId());
    }
}
