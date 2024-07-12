package aws.vpc.ecr;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class EcrAdminister {

    public void createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        EcrRepositoryConfigurator ecrRepositoryConfigurator = new EcrRepositoryConfigurator(stack);
        ecrRepositoryConfigurator.configure("auto-study-app-ver2", 10);
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "EcrStack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
