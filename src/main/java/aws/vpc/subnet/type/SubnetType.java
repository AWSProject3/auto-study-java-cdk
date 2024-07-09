package aws.vpc.subnet.type;

public enum SubnetType {

    PUBLIC_TYPE("public"),
    PRIVATE_TYPE("private");

    private final String value;

    SubnetType(String value) {
        this.value = value;
    }
}
