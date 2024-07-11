import aws.vpc.BasicInfraAdminister;
import aws.vpc.VpcInfraManager;
import aws.vpc.dto.BasicInfraDto;
import aws.vpc.rds.RdsAdminister;
import aws.vpc.s3.S3Administer;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.SubnetType;
import java.util.List;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    private static final String ACCOUNT_ID = "730335599027";
    private static final String REGION = "us-east-2";

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        VpcInfraManager vpcInfraManager = infraAdminister.createInfra(app, ACCOUNT_ID, REGION);
//        sendToEksStack(vpcInfraManager, app);

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        S3Administer s3Administer = new S3Administer();
        s3Administer.createInfra(app, ACCOUNT_ID, REGION);

//        EksAdminister eksAdminister = new EksAdminister();
//        eksAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        System.out.println("complete");

        app.synth();
    }

    private static void sendToEksStack(VpcInfraManager vpcInfraManager, App app) {
        BasicInfraDto infraDto = vpcInfraManager.getInfraDto();

        app.getNode().setContext("vpcId", infraDto.vpcId());
        app.getNode().setContext("publicSubnetIds", findPublicSubnetIds(infraDto));
        app.getNode().setContext("privateSubnetIds", findPrivateSubnetIds(infraDto));
    }

    private static List<String> findPublicSubnetIds(BasicInfraDto infraDto) {
        return infraDto.subnetDtos().stream()
                .filter(subnetDto -> subnetDto.type() == SubnetType.PUBLIC_TYPE)
                .map(SubnetDto::id).toList();
    }

    private static List<String> findPrivateSubnetIds(BasicInfraDto infraDto) {
        return infraDto.subnetDtos().stream()
                .filter(subnetDto -> subnetDto.type() == SubnetType.PRIVATE_TYPE)
                .map(SubnetDto::id).toList();
    }
}
