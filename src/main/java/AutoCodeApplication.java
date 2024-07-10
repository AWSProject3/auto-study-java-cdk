import aws.eks.EksAdminister;
import aws.rds.RdsAdminister;
import aws.vpc.BasicInfraAdminister;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        infraAdminister.createInfra(app, "730335599027", "us-east-2");

        EksAdminister eksAdminister = new EksAdminister();
        eksAdminister.createInfra(app, "730335599027", "us-east-2");

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, "730335599027", "us-east-2");

        System.out.println("complete");

        app.synth();
    }
}
