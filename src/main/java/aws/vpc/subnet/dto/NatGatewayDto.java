package aws.vpc.subnet.dto;

public class NatGatewayDto {
    private String id;

    public NatGatewayDto(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
