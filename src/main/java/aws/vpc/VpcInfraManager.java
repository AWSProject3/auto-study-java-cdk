package aws.vpc;

import aws.vpc.dto.BasicInfraDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.AzType;
import java.util.Arrays;
import java.util.List;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.constructs.Construct;

public record VpcInfraManager(BasicInfraDto infraDto) {
    private static final String DEFAULT_VPC_ID = "your-vpc-id";

    public IVpc findExistingVpc(Construct scope) {
        return Vpc.fromVpcAttributes(scope, DEFAULT_VPC_ID, VpcAttributes.builder()
                .vpcId(infraDto.vpcId())
                .availabilityZones(getAvailabilityZones())
                .privateSubnetIds(findPrivateSubnetIds())
                .build());
    }

    private List<String> getAvailabilityZones() {
        return Arrays.stream(AzType.values()).map(AzType::getValue).toList();
    }

    private List<String> findPrivateSubnetIds() {
        return infraDto.subnetDtos().stream()
                .filter(subnetDto -> subnetDto.type() == aws.vpc.type.SubnetType.PRIVATE_TYPE)
                .map(SubnetDto::id)
                .toList();
    }

    public SubnetSelection createPrivateSubnetSelector(Construct scope) {
        return SubnetSelection.builder()
                .subnets(findPrivateSubnets(scope))
                .build();
    }

    private List<ISubnet> findPrivateSubnets(Construct scope) {
        return findPrivateSubnetIds().stream()
                .map(id -> Subnet.fromSubnetId(scope, "PrivateSubnet" + id, id))
                .toList();
    }
}
