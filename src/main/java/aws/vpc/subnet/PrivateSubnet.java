package aws.vpc.subnet;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.AzType;
import aws.vpc.type.SubnetType;
import java.util.Map;
import software.amazon.awscdk.Tags;
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

    public SubnetDto configSubnet(String subnetId, String cidr, AzType az, Map<String, String> tags) {
        CfnSubnet privateSubnet = createPrivateSubnet(subnetId, cidr, az);
        applyTags(privateSubnet, tags);
        return new SubnetDto(SubnetType.PRIVATE_TYPE, privateSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPrivateSubnet(String subnetId, String cidr, AzType az) {
        return Builder.create(scope, subnetId + az.getValue())
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(az.getValue())
                .mapPublicIpOnLaunch(false)
                .build();
    }

    private void applyTags(CfnSubnet subnet, Map<String, String> tags) {
        tags.forEach((key, value) -> Tags.of(subnet).add(key, value));
    }
}
