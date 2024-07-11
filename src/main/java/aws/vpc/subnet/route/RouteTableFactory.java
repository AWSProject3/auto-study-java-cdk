package aws.vpc.subnet.route;

import static aws.vpc.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.ngw.NatGatewayConfigurator;
import aws.vpc.type.AzType;
import java.util.List;
import java.util.Optional;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class RouteTableFactory {

    private static final String PUBLIC_ROUTE_TABLE_PREFIX = "PublicRouteTable";
    private static final String PUBLIC_ROUTE_PREFIX = "PublicRoute";
    private static final String PRIVATE_ROUTE_TABLE_PREFIX = "PrivateRouteTable";
    private static final String PRIVATE_ROUTE_PREFIX = "PrivateRoute";

    private final Construct scope;
    private final Vpc vpc;
    private final List<SubnetDto> subnetDtos;

    public RouteTableFactory(Construct scope, Vpc vpc, List<SubnetDto> subnetDtos) {
        this.scope = scope;
        this.vpc = vpc;
        this.subnetDtos = subnetDtos;
    }

    public RouteTable createRouteTable(SubnetDto subnetDto, String igwId) {
        String suffix = createSuffix(subnetDto);
        if (isPublicSubnet(subnetDto)) {
            return createPublicRouteTable(suffix, igwId);
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

    private RouteTable createPublicRouteTable(String suffix, String igwId) {
        String publicRouteTableId = PUBLIC_ROUTE_TABLE_PREFIX + suffix;
        String publicRouteId = PUBLIC_ROUTE_PREFIX + suffix;
        return new PublicRouteTable(scope, vpc, publicRouteTableId, publicRouteId, igwId);
    }

    private RouteTable createPrivateRouteTable(String suffix, SubnetDto subnetDto) {
        String privateRouteTableId = PRIVATE_ROUTE_TABLE_PREFIX + suffix;
        String privateRouteId = PRIVATE_ROUTE_PREFIX + suffix;
        return new PrivateRouteTable(scope, vpc, privateRouteTableId, privateRouteId, createOptionalNgw(subnetDto));
    }

    private Optional<NatGatewayDto> createOptionalNgw(SubnetDto subnetDto) {
        return findRelatedPublicSubnet(subnetDto).map(this::createNatGateway);
    }

    private Optional<SubnetDto> findRelatedPublicSubnet(SubnetDto subnetDto) {
        AzType az = subnetDto.az();
        return subnetDtos.stream().filter(subnet -> subnet.az() == az && subnet.type() == PUBLIC_TYPE).findFirst();
    }

    private NatGatewayDto createNatGateway(SubnetDto subnetDto) {
        NatGatewayConfigurator natGatewayConfigurator = new NatGatewayConfigurator(scope);
        return natGatewayConfigurator.configure(subnetDto);
    }
}
