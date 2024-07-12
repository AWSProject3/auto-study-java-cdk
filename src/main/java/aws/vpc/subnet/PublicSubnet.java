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

public class PublicSubnet {

    private final Construct scope;
    private final Vpc vpc;

    public PublicSubnet(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public SubnetDto configSubnet(String subnetId, String cidr, AzType az, Map<String, String> tags) {
        CfnSubnet publicSubnet = createPublicSubnet(subnetId, cidr, az);
        applyTags(publicSubnet, tags);
        return new SubnetDto(SubnetType.PUBLIC_TYPE, publicSubnet.getAttrSubnetId(), az);
    }

    private CfnSubnet createPublicSubnet(String subnetId, String cidr, AzType az) {
        return Builder.create(scope, subnetId)
                .vpcId(vpc.getVpcId())
                .cidrBlock(cidr)
                .availabilityZone(az.getValue())
                .mapPublicIpOnLaunch(true)
                .build();
    }

    private void applyTags(CfnSubnet subnet, Map<String, String> tags) {
        tags.forEach((key, value) -> Tags.of(subnet).add(key, value));
    }
}
