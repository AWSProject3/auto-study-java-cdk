package aws.vpc.subnet.type;


public enum AZType {

    AZ_1A("us-east-1a"),
    AZ_1B("us-east-1b");

    private final String value;

    AZType(String value) {
        this.value = value;
    }

    public boolean existsFirstAZ() {
        return this.equals(AZ_1A);
    }

    public String getValue() {
        return this.value;
    }
}
