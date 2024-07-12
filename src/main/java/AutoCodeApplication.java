import aws.vpc.BasicInfraAdminister;
import aws.vpc.VpcInfraManager;
import aws.vpc.dto.BasicInfraDto;
import aws.vpc.rds.RdsAdminister;
import aws.vpc.s3.S3Administer;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.AzType;
import aws.vpc.type.SubnetType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    private static final String ACCOUNT_ID = "730335599027";
    private static final String REGION = "us-east-2";

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        VpcInfraManager vpcInfraManager = infraAdminister.createInfra(app, ACCOUNT_ID, REGION);

        String vpcInfo = convertVpcInfoToJson(vpcInfraManager);
        System.setProperty("VPC_INFO", vpcInfo);

        RdsAdminister rdsAdminister = new RdsAdminister();
        rdsAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        S3Administer s3Administer = new S3Administer();
        s3Administer.createInfra(app, ACCOUNT_ID, REGION);

//        EksAdminister eksAdminister = new EksAdminister();
//        eksAdminister.createInfra(app, ACCOUNT_ID, REGION, vpcInfraManager);

        System.out.println("complete");

        app.synth();
    }

    private static String convertVpcInfoToJson(VpcInfraManager vpcInfraManager) {
        BasicInfraDto infraDto = vpcInfraManager.getInfraDto();
        Map<String, Object> vpcInfo = new HashMap<>();
        vpcInfo.put("vpcId", infraDto.vpcId());
        vpcInfo.put("publicSubnetIds", findPublicSubnetIds(infraDto));
        vpcInfo.put("privateSubnetIds", findPrivateSubnetIds(infraDto));
        vpcInfo.put("availabilityZones", List.of(AzType.AZ_1A, AzType.AZ_1B));

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(vpcInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting VPC info to JSON", e);
        }
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
