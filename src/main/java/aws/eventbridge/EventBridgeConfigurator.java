package aws.eventbridge;

import java.util.Collections;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

public class EventBridgeConfigurator {
    private final Construct scope;
    private final LambdaGenerator lambdaGenerator;

    public EventBridgeConfigurator(Construct scope, LambdaGenerator lambdaGenerator) {
        this.scope = scope;
        this.lambdaGenerator = lambdaGenerator;
    }

    public void configure() {
        Function lambda = lambdaGenerator.createLambdaWithExecutionRole();
        Rule eventBridgeRule = createEventBridgeRule();
        eventBridgeRule.addTarget(new LambdaFunction(lambda));
    }

    private Rule createEventBridgeRule() {
        return Rule.Builder.create(scope, "EcrPushRule")
                .eventPattern(EventPattern.builder()
                        .source(Collections.singletonList("aws.ecr"))
                        .detailType(Collections.singletonList("ECR Image Action"))
                        .detail(Collections.singletonMap("action-type",
                                Collections.singletonList("PUSH")))
                        .build())
                .build();
    }
}
