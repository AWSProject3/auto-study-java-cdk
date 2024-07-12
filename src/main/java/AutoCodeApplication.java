import aws.vpc.BasicInfraAdminister;
import aws.vpc.VpcInfraManager;
import aws.vpc.dto.BasicInfraDto;
import aws.vpc.rds.RdsAdminister;
import aws.vpc.s3.S3Administer;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.SubnetType;
import java.util.List;
import software.amazon.awscdk.App;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

public class AutoCodeApplication {

    private static final String ACCOUNT_ID = "730335599027";
    private static final String REGION = "us-east-2";

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        VpcInfraManager vpcInfraManager = infraAdminister.createInfra(app, ACCOUNT_ID, REGION);

//        BasicInfraDto infraDto = vpcInfraManager.infraDto();
//        String vpcId = infraDto.vpcId();
//        List<String> publicSubnetIds = findPublicSubnetIds(infraDto);
//        List<String> privateSubnetIds = findPrivateSubnetIds(infraDto);
//        List<String> availabilityZones = Arrays.stream(AzType.values()).map(AzType::getValue).toList();
//
//        storeParameterInSSM("/eks-config/vpcId", vpcId);
//        storeParameterInSSM("/eks-config/publicSubnetIds", String.join(",", publicSubnetIds));
//        storeParameterInSSM("/eks-config/privateSubnetIds", String.join(",", privateSubnetIds));
//        storeParameterInSSM("/eks-config/availabilityZones", String.join(",", availabilityZones));

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        S3Administer s3Administer = new S3Administer();
        s3Administer.createInfra(app, ACCOUNT_ID, REGION);

        System.out.println("complete");

        app.synth();
    }

    private static void storeParameterInSSM(String parameterName, String parameterValue) {
        SsmClient ssmClient = SsmClient.builder().build();
        PutParameterRequest request = PutParameterRequest.builder()
                .name(parameterName)
                .value(parameterValue)
                .type("String")
                .overwrite(true)
                .build();
        ssmClient.putParameter(request);
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
