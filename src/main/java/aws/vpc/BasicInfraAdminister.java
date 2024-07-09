package aws.vpc;

import aws.vpc.subnet.SubnetConfig;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.route.RouteTableConfig;
import java.util.List;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

public class BasicInfraAdminister {

    public void createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack scope = new Stack(app, "VpcStack", StackProps.builder().env(env).build());

        VpcConfig vpcConfig = new VpcConfig(scope);
        vpcConfig.configure("auto-study-vpc");

        SubnetConfig subnetConfig = new SubnetConfig(scope);
        List<SubnetDto> subnetDtos = subnetConfig.configure();

        IgwConfig igwConfig = new IgwConfig(scope);
        igwConfig.configure("igw");

        RouteTableConfig routeTableConfig = new RouteTableConfig(scope);
        routeTableConfig.configure(subnetDtos);
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
