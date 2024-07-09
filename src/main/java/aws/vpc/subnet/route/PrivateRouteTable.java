package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import java.util.Optional;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.Vpc;

public class PrivateRouteTable implements RouteTable {
    private static final String CIDR = "0.0.0.0/0";

    private final Construct scope;
    private final Vpc vpc;
    private final String routeTableId;
    private final Optional<NatGatewayDto> natGateway;

    public PrivateRouteTable(Construct scope, Vpc vpc, String routeTableId, Optional<NatGatewayDto> natGateway) {
        this.scope = scope;
        this.vpc = vpc;
        this.routeTableId = routeTableId;
        this.natGateway = natGateway;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        CfnRouteTable routeTable = createRouteTable();
        routePrivateSubnetToNatGateway(routeTable);
        associateWithSubnet(subnetDto, routeTable);
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private void routePrivateSubnetToNatGateway(CfnRouteTable routeTable) {
        String routeId = "PrivateRoute" + findRouteTableOrder(routeTable);
        natGateway.ifPresentOrElse(
                ngw -> assignRoute(routeTable, routeId, ngw),
                () -> assignRoute(routeTable, routeId)
        );
    }

    private String findRouteTableOrder(CfnRouteTable routeTable) {
        if ("PrivateRouteTable1".equals(routeTable.getAttrRouteTableId())) {
            return "1";
        }
        return "2";
    }

    private void assignRoute(CfnRouteTable routeTable, String routeId, NatGatewayDto natGateway) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .destinationCidrBlock(CIDR)
                .natGatewayId(natGateway.getId())
                .build();
    }

    private void assignRoute(CfnRouteTable routeTable, String routeId) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .destinationCidrBlock(CIDR)
                .build();
    }

    private CfnSubnetRouteTableAssociation associateWithSubnet(SubnetDto subnetDto, CfnRouteTable routeTable) {
        if (subnetDto.getAz().existsFirstAZ()) {
            return createAssociation(subnetDto.getId(), routeTable, "PrivateSubnet1RouteTableAssociation");
        }
        return createAssociation(subnetDto.getId(), routeTable, "PrivateSubnet2RouteTableAssociation");
    }

    private CfnSubnetRouteTableAssociation createAssociation(String subnetId, CfnRouteTable routeTable, String id) {
        return CfnSubnetRouteTableAssociation.Builder.create(scope, id)
                .subnetId(subnetId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .build();
    }
}
