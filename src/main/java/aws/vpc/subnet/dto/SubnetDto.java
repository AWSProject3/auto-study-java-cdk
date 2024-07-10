package aws.vpc.subnet.dto;

import aws.vpc.type.AzType;
import aws.vpc.type.SubnetType;

public record SubnetDto(SubnetType type, String id, AzType az) {

}
