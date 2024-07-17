import aws.ecr.EcrAdminister;
import aws.eventbridge.EventBridgeAdminister;
import aws.s3.S3Administer;
import aws.vpc.BasicInfraAdminister;
import aws.vpc.VpcInfraManager;
import aws.vpc.elasticcache.RedisAdminister;
import aws.vpc.rds.RdsAdminister;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    private static final String ACCOUNT_ID = "471112903915";
    private static final String REGION = "us-east-1";

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        VpcInfraManager vpcInfraManager = infraAdminister.createInfra(app, ACCOUNT_ID, REGION);

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        RedisAdminister redisAdminister = new RedisAdminister();
        redisAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        S3Administer s3Administer = new S3Administer();
        s3Administer.createInfra(app, ACCOUNT_ID, REGION);

        EcrAdminister ecrAdminister = new EcrAdminister();
        ecrAdminister.createInfra(app, ACCOUNT_ID, REGION);

        EventBridgeAdminister eventBridgeAdminister = new EventBridgeAdminister();
        eventBridgeAdminister.createInfra(app, ACCOUNT_ID, REGION);

        app.synth();
    }
}
