package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;

public interface RouteTable {
    void configure(SubnetDto subnetDto);
}
