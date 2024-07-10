import aws.vpc.BasicInfraAdminister;
import aws.vpc.common.VpcInfraManager;
import aws.vpc.eks.EksAdminister;
import aws.vpc.rds.RdsAdminister;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        VpcInfraManager vpcInfraManager = infraAdminister.createInfra(app, "730335599027", "us-east-2");

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, "730335599027", "us-east-2", vpcInfraManager);

        EksAdminister eksAdminister = new EksAdminister();
        eksAdminister.createInfra(app, "730335599027", "us-east-2", vpcInfraManager);

        System.out.println("complete");

        app.synth();
    }
    // action
}
