package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import java.util.Optional;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class PrivateRouteTable implements RouteTable {
    private static final String CIDR = "0.0.0.0/0";

    private final Construct scope;
    private final Vpc vpc;
    private final String routeTableId;
    private final String privateRouteId;
    private final Optional<NatGatewayDto> natGateway;

    public PrivateRouteTable(Construct scope, Vpc vpc, String routeTableId, String privateRouteId,
                             Optional<NatGatewayDto> natGateway) {
        this.scope = scope;
        this.vpc = vpc;
        this.routeTableId = routeTableId;
        this.privateRouteId = privateRouteId;
        this.natGateway = natGateway;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        String createdRouteTableId = createRouteTable().getAttrRouteTableId();
        associateRouteTableWithSubnet(subnetDto, createdRouteTableId);
        routePrivateSubnetToNatGateway(createdRouteTableId);
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private CfnSubnetRouteTableAssociation associateRouteTableWithSubnet(SubnetDto subnetDto, String routeTableId) {
        if (subnetDto.az().existsFirstAZ()) {
            return createAssociation(subnetDto.id(), routeTableId, "PrivateSubnet1RouteTableAssociation");
        }
        return createAssociation(subnetDto.id(), routeTableId, "PrivateSubnet2RouteTableAssociation");
    }

    private CfnSubnetRouteTableAssociation createAssociation(String subnetId, String routeTableId,
                                                             String associationId) {
        return CfnSubnetRouteTableAssociation.Builder.create(scope, associationId)
                .subnetId(subnetId)
                .routeTableId(routeTableId)
                .build();
    }

    private void routePrivateSubnetToNatGateway(String routeTableId) {
        natGateway.ifPresent(ngw -> assignRoute(privateRouteId, routeTableId, ngw));
    }

    private void assignRoute(String routeId, String routeTableId, NatGatewayDto natGateway) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTableId)
                .destinationCidrBlock(CIDR)
                .natGatewayId(natGateway.id())
                .build();
    }
}
