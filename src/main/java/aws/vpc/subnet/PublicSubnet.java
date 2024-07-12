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

public class PublicSubnet {

    private final Construct scope;
    private final Vpc vpc;

    public PublicSubnet(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public SubnetDto configSubnet(String subnetId, String cidr, AzType az) {
        CfnSubnet publicSubnet = createPublicSubnet(subnetId, cidr, az);
        return new SubnetDto(SubnetType.PUBLIC_TYPE, publicSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPublicSubnet(String subnetId, String cidr, AzType az) {
        CfnSubnet subnet = Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(az.getValue())
                .mapPublicIpOnLaunch(true)
                .build();

        new CfnOutput(scope, "PublicSubnetId" + az, CfnOutputProps.builder()
                .value(subnet.getAttrSubnetId())
                .exportName("publicSubnetId" + az.getValue())
                .build());

        return subnet;
    }
}
