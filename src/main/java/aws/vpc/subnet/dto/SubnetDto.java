package aws.vpc.subnet.dto;

import aws.vpc.subnet.type.AZType;
import aws.vpc.subnet.type.SubnetType;

public record SubnetDto(SubnetType type, String id, AZType az) {

}
