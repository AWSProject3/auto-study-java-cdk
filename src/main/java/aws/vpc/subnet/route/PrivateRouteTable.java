package aws.vpc.subnet.route;

import aws.vpc.subnet.NatGatewayConfig;
import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.util.ScopeValidator;
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

    public PrivateRouteTable(Construct scope, String routeTableId) {
        this.scope = scope;
        this.vpc = ScopeValidator.extractVpcBy(scope);
        this.routeTableId = routeTableId;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        CfnRouteTable routeTable = createRouteTable();
        allocateNatGatewayToPrivateSubnet(subnetDto, routeTable);
        associateWithSubnet(subnetDto, routeTable);
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private void allocateNatGatewayToPrivateSubnet(SubnetDto subnetDto, CfnRouteTable routeTable) {
        NatGatewayDto natGateway = createNatGateway(subnetDto);
        assignRoute(routeTable, natGateway.getId(), "PrivateRoute" + findRouteTableOrder(routeTable));
    }

    private NatGatewayDto createNatGateway(SubnetDto subnetDto) {
        NatGatewayConfig natGatewayConfig = new NatGatewayConfig(scope);
        return natGatewayConfig.configure(subnetDto);
    }

    private String findRouteTableOrder(CfnRouteTable routeTable) {
        if ("PrivateRouteTable1".equals(routeTable.getAttrRouteTableId())) {
            return "1";
        }
        return "2";
    }

    private void assignRoute(CfnRouteTable routeTable, String natGatewayId, String routeId) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .destinationCidrBlock(CIDR)
                .natGatewayId(natGatewayId)
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
