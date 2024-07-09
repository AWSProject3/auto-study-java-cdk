package aws.vpc.subnet.dto;

import aws.vpc.subnet.type.AZType;
import aws.vpc.subnet.type.SubnetType;

public class SubnetDto {

    private SubnetType type;
    private String id;
    private AZType az;

    public SubnetDto(SubnetType type, String id, AZType az) {
        this.type = type;
        this.id = id;
        this.az = az;
    }

    public SubnetType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public AZType getAz() {
        return az;
    }
}
