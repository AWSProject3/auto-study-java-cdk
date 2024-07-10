package aws.vpc.subnet.type;


public enum AzType {

    AZ_1A("us-east-2a"),
    AZ_1B("us-east-2b");

    private final String value;

    AzType(String value) {
        this.value = value;
    }

    public boolean existsFirstAZ() {
        return this.equals(AZ_1A);
    }

    public String getValue() {
        return this.value;
    }
}
