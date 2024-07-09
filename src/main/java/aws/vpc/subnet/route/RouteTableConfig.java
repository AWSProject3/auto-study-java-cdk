package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;
import java.util.List;
import software.amazon.awscdk.core.Construct;

public class RouteTableConfig {
    private final Construct scope;
    private final RouteTableFactory routeTableFactory;

    public RouteTableConfig(Construct scope) {
        this.scope = scope;
        this.routeTableFactory = new RouteTableFactory(scope);
    }

    public void configure(List<SubnetDto> subnetDtos) {
        subnetDtos.forEach(subnetDto -> routeTableFactory.createRouteTable(subnetDto).configure(subnetDto));
    }
}
