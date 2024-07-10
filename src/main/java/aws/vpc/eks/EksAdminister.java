package aws.vpc.eks;

import aws.vpc.common.VpcInfraManager;
import aws.vpc.eks.ecr.EcrConfig;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class EksAdminister {

    public void createInfra(App app, String account, String region, VpcInfraManager vpcInfraManager) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        EksConfig eksConfig = new EksConfig(stack, vpcInfraManager);
        eksConfig.configure("auto-study-eks");

        EcrConfig ecrConfig = new EcrConfig(stack);
        ecrConfig.configure("auto-study-app", 10);
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "EksBlueprintStack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
