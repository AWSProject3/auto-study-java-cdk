package aws.vpc;

import aws.vpc.dto.BasicInfraDto;
import aws.vpc.igw.IgwConfigurator;
import aws.vpc.subnet.SubnetConfigurator;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.subnet.route.RouteTableConfigurator;
import java.util.List;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;

public class BasicInfraAdminister {

    public VpcInfraManager createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack scope = new Stack(app, "VpcStack", StackProps.builder().env(env).build());

        VpcConfigurator vpcConfigurator = new VpcConfigurator(scope);
        Vpc vpc = vpcConfigurator.configureEmptyVpc("auto-study-vpc");

        SubnetConfigurator subnetConfigurator = new SubnetConfigurator(scope, vpc);
        List<SubnetDto> subnetDtos = subnetConfigurator.configure();

        IgwConfigurator igwConfigurator = new IgwConfigurator(scope, vpc);
        String igwId = igwConfigurator.configure("igw");

        RouteTableConfigurator routeTableConfigurator = new RouteTableConfigurator(scope, vpc, subnetDtos, igwId);
        routeTableConfigurator.configure();

        return new VpcInfraManager(new BasicInfraDto(subnetDtos, vpc.getVpcId()));
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
