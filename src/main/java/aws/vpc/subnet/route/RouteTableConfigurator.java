package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;
import java.util.List;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class RouteTableConfigurator {
    private final List<SubnetDto> subnetDtos;
    private final String igwId;
    private final RouteTableFactory routeTableFactory;

    public RouteTableConfigurator(Construct scope, Vpc vpc, List<SubnetDto> subnetDtos, String igwId) {
        this.subnetDtos = subnetDtos;
        this.igwId = igwId;
        this.routeTableFactory = new RouteTableFactory(scope, vpc, subnetDtos);
    }

    public void configure() {
        subnetDtos.forEach(subnetDto -> routeTableFactory.createRouteTable(subnetDto, igwId).configure(subnetDto));
    }
}
