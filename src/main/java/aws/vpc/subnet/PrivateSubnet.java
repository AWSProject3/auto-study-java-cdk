package aws.vpc.subnet;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.AzType;
import aws.vpc.type.SubnetType;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.CfnSubnet.Builder;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class PrivateSubnet {

    private final Construct scope;
    private final Vpc vpc;

    public PrivateSubnet(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public SubnetDto configSubnet(String subnetId, String cidr, AzType az) {
        CfnSubnet privateSubnet = createPrivateSubnet(subnetId, cidr, az);
        return new SubnetDto(SubnetType.PRIVATE_TYPE, privateSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPrivateSubnet(String subnetId, String cidr, AzType az) {
        CfnSubnet subnet = Builder.create(scope, subnetId + az.getValue())
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(az.getValue())
                .mapPublicIpOnLaunch(false)
                .build();

        new CfnOutput(scope, "PrivateSubnetId" + az , CfnOutputProps.builder()
                .value(subnet.getAttrSubnetId())
                .exportName("MyPrivateSubnetId" + az.getValue())
                .build());

        return subnet;
    }
}
