package aws.vpc.subnet.dto;

import java.util.List;

public record BasicInfraDto(List<SubnetDto> subnetDtos, String vpcId) {

}
