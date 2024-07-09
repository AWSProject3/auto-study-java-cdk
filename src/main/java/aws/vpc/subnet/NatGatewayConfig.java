package aws.vpc.subnet;


import static aws.vpc.subnet.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnEIP.Builder;
import software.amazon.awscdk.services.ec2.CfnNatGateway;

public class NatGatewayConfig {

    private static final String EIP = "EIP1";
    private static final String ID_PREFIX = "NateGateWay";

    private final Construct scope;

    public NatGatewayConfig(Construct scope) {
        this.scope = scope;
    }

//    public List<NatGatewayDto> configure(List<SubnetDto> subnetDtos) {
//        return subnetDtos.stream()
//                .filter(this::isPublicType)
//                .map(this::createNatGateway)
//                .map(natGateway -> new NatGatewayDto(natGateway.getAttrNatGatewayId()))
//                .toList();
//    }

    public NatGatewayDto configure(SubnetDto subnetDto) {
        if (isPublicType(subnetDto)) {
            return createNatGatewayDto(subnetDto);
        }
        throw new RuntimeException("Natgateway를 생성하려 하는 서브넷이 private 입니다.");
    }

    private boolean isPublicType(SubnetDto subnetDto) {
        return PUBLIC_TYPE.equals(subnetDto.type());
    }

    private NatGatewayDto createNatGatewayDto(SubnetDto subnetDto) {
        int suffix = createSuffix(subnetDto);
        CfnNatGateway natGateway = createNatGateway(subnetDto.id(), createEIP(EIP + suffix), ID_PREFIX + suffix);
        return new NatGatewayDto(natGateway.getAttrNatGatewayId());
    }

    private int createSuffix(SubnetDto subnetDto) {
        if (subnetDto.az().existsFirstAZ()) {
            return 1;
        }
        return 2;
    }

    private CfnNatGateway createNatGateway(String subnetId, CfnEIP eip, String id) {
        return CfnNatGateway.Builder.create(scope, id)
                .subnetId(subnetId)
                .allocationId(eip.getAttrAllocationId())
                .build();
    }

    private CfnEIP createEIP(String id) {
        return Builder.create(scope, id).build();
    }
}
