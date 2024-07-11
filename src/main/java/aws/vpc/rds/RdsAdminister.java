package aws.vpc.rds;

import aws.vpc.VpcInfraManager;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class RdsAdminister {

    public void createInfra(App app, String account, String region, VpcInfraManager vpcInfraManager) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        RdsConfigurator rdsConfigurator = new RdsConfigurator(stack, vpcInfraManager);
        rdsConfigurator.configure("rds-instance", "AutoStudyRds", "admin", "0912dltncks!VC");
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "RdsStack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
