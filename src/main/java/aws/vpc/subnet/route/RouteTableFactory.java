package aws.vpc.subnet.route;

import static aws.vpc.subnet.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.NatGatewayConfig;
import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import java.util.Optional;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.Vpc;

public class RouteTableFactory {

    private static final String PUBLIC_ROUTE_TABLE_PREFIX = "PublicRouteTable";
    private static final String PUBLIC_ROUTE_PREFIX = "PublicRoute";
    private static final String PRIVATE_ROUTE_TABLE_PREFIX = "PrivateRouteTable";
    private static final String PRIVATE_ROUTE_PREFIX = "PrivateRoute";

    private static final String TARGET = "PrivateSubnet";
    private static final String REPLACEMENT = "PublicSubnet";

    private final Construct scope;
    private final Vpc vpc;

    public RouteTableFactory(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public RouteTable createRouteTable(SubnetDto subnetDto) {
        String suffix = createSuffix(subnetDto);
        if (isPublicSubnet(subnetDto)) {
            return createPublicRouteTable(suffix);
        }
        return createPrivateRouteTable(suffix, subnetDto);
    }

    private String createSuffix(SubnetDto subnetDto) {
        if (subnetDto.az().existsFirstAZ()) {
            return "1";
        }
        return "2";
    }

    private boolean isPublicSubnet(SubnetDto subnetDto) {
        return PUBLIC_TYPE.equals(subnetDto.type());
    }

    private RouteTable createPublicRouteTable(String suffix) {
        String publicRouteTableId = PUBLIC_ROUTE_TABLE_PREFIX + suffix;
        String publicRouteId = PUBLIC_ROUTE_PREFIX + suffix;
        return new PublicRouteTable(scope, vpc, publicRouteTableId, publicRouteId);
    }

    private RouteTable createPrivateRouteTable(String suffix, SubnetDto subnetDto) {
        String privateRouteTableId = PRIVATE_ROUTE_TABLE_PREFIX + suffix;
        String privateRouteId = PRIVATE_ROUTE_PREFIX + suffix;
        return new PrivateRouteTable(scope, vpc, privateRouteTableId, privateRouteId, createOptionalNgw(subnetDto));
    }

    private Optional<NatGatewayDto> createOptionalNgw(SubnetDto subnetDto) {
        return hasRelatedPublicSubnet(subnetDto) ? Optional.of(createNatGateway(subnetDto)) : Optional.empty();
    }

    private boolean hasRelatedPublicSubnet(SubnetDto subnetDto) {
        String privateSubnetId = subnetDto.id();
        String relatedPublicSubnetId = privateSubnetId.replace(TARGET, REPLACEMENT);

        return vpc.getPublicSubnets().stream()
                .anyMatch(subnet -> subnet.getSubnetId().equals(relatedPublicSubnetId));
    }

    private NatGatewayDto createNatGateway(SubnetDto subnetDto) {
        NatGatewayConfig natGatewayConfig = new NatGatewayConfig(scope);
        return natGatewayConfig.configure(subnetDto);
    }
}
