import aws.vpc.BasicInfraAdminister;
import software.amazon.awscdk.core.App;

public class AutoCodeApplication {

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        infraAdminister.createInfra(app, "730335599027", "us-east-1");

        // rds
        // eks

        app.synth();
    }
}
