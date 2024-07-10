package aws.vpc;

import aws.vpc.igw.IgwConfig;
import aws.vpc.subnet.SubnetConfig;
import aws.vpc.subnet.dto.BasicInfraDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.route.RouteTableConfig;
import java.util.List;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;

public class BasicInfraAdminister {

    public BasicInfraDto createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack scope = new Stack(app, "VpcStack", StackProps.builder().env(env).build());

        VpcConfig vpcConfig = new VpcConfig(scope);
        Vpc vpc = vpcConfig.configureEmptyVpc("auto-study-vpc");

        SubnetConfig subnetConfig = new SubnetConfig(scope, vpc);
        List<SubnetDto> subnetDtos = subnetConfig.configure();

        IgwConfig igwConfig = new IgwConfig(scope, vpc);
        String igwId = igwConfig.configure("igw");

        RouteTableConfig routeTableConfig = new RouteTableConfig(scope, vpc, subnetDtos, igwId);
        routeTableConfig.configure();

        return new BasicInfraDto(subnetDtos, vpc.getVpcId());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
