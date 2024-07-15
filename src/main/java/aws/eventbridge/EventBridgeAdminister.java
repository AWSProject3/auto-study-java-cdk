package aws.eventbridge;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class EventBridgeAdminister {

    public void createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        EventBridgeConfigurator eventBridgeConfigurator = new EventBridgeConfigurator(stack,
                new LambdaConfigurator(stack));
        eventBridgeConfigurator.configure();
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "EventBridgeStack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
