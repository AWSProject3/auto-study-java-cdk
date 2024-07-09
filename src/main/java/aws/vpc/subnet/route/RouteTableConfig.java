package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;
import java.util.List;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.Vpc;

public class RouteTableConfig {
    private final Construct scope;
    private final RouteTableFactory routeTableFactory;

    public RouteTableConfig(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.routeTableFactory = new RouteTableFactory(scope, vpc);
    }

    public void configure(List<SubnetDto> subnetDtos) {
        subnetDtos.forEach(subnetDto -> routeTableFactory.createRouteTable(subnetDto).configure(subnetDto));
    }
}
