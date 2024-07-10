package aws.vpc.subnet.dto;

import aws.vpc.subnet.type.AzType;
import aws.vpc.subnet.type.SubnetType;

public record SubnetDto(SubnetType type, String id, AzType az) {

}
