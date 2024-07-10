package aws.eks;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class EksAdminister {

    public void createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        EksConfig eksConfig = new EksConfig(stack);
        eksConfig.configure("auto-study-eks");
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
