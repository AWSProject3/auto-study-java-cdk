package aws.vpc.dto;

import aws.vpc.subnet.dto.SubnetDto;
import java.util.List;

public record BasicInfraDto(List<SubnetDto> subnetDtos, String vpcId) {

}
