package aws.vpc.subnet.route;

import static aws.vpc.subnet.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.dto.SubnetDto;
import software.amazon.awscdk.core.Construct;

public class RouteTableFactory {

    private static final String PUBLIC_ROUTE_TABLE_PREFIX = "PublicRouteTable";
    private static final String PUBLIC_ROUTE_PREFIX = "PublicRoute";
    private static final String PRIVATE_ROUTE_TABLE_PREFIX = "PrivateRouteTable";

    private final Construct scope;

    public RouteTableFactory(Construct scope) {
        this.scope = scope;
    }

    public RouteTable createRouteTable(SubnetDto subnetDto) {
        String suffix = createSuffix(subnetDto);
        if (isPublicSubnet(subnetDto)) {
            return createPublicRouteTable(suffix);
        }
        return createPrivateRouteTable(suffix);
    }

    private String createSuffix(SubnetDto subnetDto) {
        if (subnetDto.getAz().existsFirstAZ()) {
            return "1";
        }
        return "2";
    }

    private boolean isPublicSubnet(SubnetDto subnetDto) {
        return PUBLIC_TYPE.equals(subnetDto.getType());
    }

    private RouteTable createPublicRouteTable(String suffix) {
        String publicRouteTableId = PUBLIC_ROUTE_TABLE_PREFIX + suffix;
        String publicRouteId = PUBLIC_ROUTE_PREFIX + suffix;
        return new PublicRouteTable(scope, publicRouteTableId, publicRouteId);
    }

    private RouteTable createPrivateRouteTable(String suffix) {
        String privateRouteTableId = PRIVATE_ROUTE_TABLE_PREFIX + suffix;
        return new PrivateRouteTable(scope, privateRouteTableId);
    }
}
