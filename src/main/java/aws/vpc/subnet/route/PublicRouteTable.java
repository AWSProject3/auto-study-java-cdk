package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.util.ScopeValidator;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation.Builder;
import software.amazon.awscdk.services.ec2.Vpc;

public class PublicRouteTable implements RouteTable {
    private static final String CIDR = "0.0.0.0/0";

    private final Construct scope;
    private final Vpc vpc;
    private final String routeTableId;
    private final String routeId;

    public PublicRouteTable(Construct scope, String routeTableId, String routeId) {
        this.scope = scope;
        this.vpc = ScopeValidator.extractVpcBy(scope);
        this.routeTableId = routeTableId;
        this.routeId = routeId;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        CfnRouteTable routeTable = createRouteTable();
        assignRoute(routeTable);
        associateWithSubnet(subnetDto, routeTable);
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private void assignRoute(CfnRouteTable routeTable) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .destinationCidrBlock(CIDR)
                .gatewayId(vpc.getInternetGatewayId())
                .build();
    }

    private CfnSubnetRouteTableAssociation associateWithSubnet(SubnetDto subnetDto, CfnRouteTable routeTable) {
        if (subnetDto.getAz().existsFirstAZ()) {
            return createAssociation(subnetDto.getId(), routeTable, "PublicSubnet1RouteTableAssociation");
        }
        return createAssociation(subnetDto.getId(), routeTable, "PublicSubnet2RouteTableAssociation");
    }

    private CfnSubnetRouteTableAssociation createAssociation(String subnetId, CfnRouteTable routeTable,
                                                             String associationId) {
        return Builder.create(scope, associationId)
                .subnetId(subnetId)
                .routeTableId(routeTable.getAttrRouteTableId())
                .build();
    }
}
