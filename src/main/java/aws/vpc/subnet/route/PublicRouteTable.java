package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRoute.Builder;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class PublicRouteTable implements RouteTable {
    private static final String CIDR = "0.0.0.0/0";

    private final Construct scope;
    private final Vpc vpc;
    private final String routeTableId;
    private final String routeId;
    private final String igwId;

    public PublicRouteTable(Construct scope, Vpc vpc, String routeTableId, String routeId, String igwId) {
        this.scope = scope;
        this.vpc = vpc;
        this.routeTableId = routeTableId;
        this.routeId = routeId;
        this.igwId = igwId;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        String createdRouteTableId = createRouteTable().getAttrRouteTableId();
        associateRouteTableWithSubnet(subnetDto, createdRouteTableId);
        assignRoute(createdRouteTableId);
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private CfnSubnetRouteTableAssociation associateRouteTableWithSubnet(SubnetDto subnetDto, String routeTableId) {
        if (subnetDto.az().existsFirstAZ()) {
            return createAssociation(subnetDto.id(), routeTableId, "PublicSubnet1RouteTableAssociation");
        }
        return createAssociation(subnetDto.id(), routeTableId, "PublicSubnet2RouteTableAssociation");
    }

    private CfnSubnetRouteTableAssociation createAssociation(String subnetId, String routeTableId,
                                                             String associationId) {
        return CfnSubnetRouteTableAssociation.Builder.create(scope, associationId)
                .subnetId(subnetId)
                .routeTableId(routeTableId)
                .build();
    }

    private void assignRoute(String routeTableId) {
        CfnRoute route = Builder.create(scope, routeId)
                .routeTableId(routeTableId)
                .destinationCidrBlock(CIDR)
                .gatewayId(igwId)
                .build();

        TagUtils.applyTags(route);
    }
}
