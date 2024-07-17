package aws.eventbridge;

import aws.vpc.util.TagUtils;
import java.util.Collections;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Rule.Builder;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

public class EventBridgeConfigurator {
    private final Construct scope;
    private final LambdaConfigurator lambdaConfigurator;

    public EventBridgeConfigurator(Construct scope, LambdaConfigurator lambdaConfigurator) {
        this.scope = scope;
        this.lambdaConfigurator = lambdaConfigurator;
    }

    public void configure() {
        Function lambda = lambdaConfigurator.createLambdaWithExecutionRole();
        Rule eventBridgeRule = createEventBridgeRule();
        eventBridgeRule.addTarget(new LambdaFunction(lambda));
    }

    private Rule createEventBridgeRule() {
        Rule rule = Builder.create(scope, "EcrPushRule")
                .eventPattern(EventPattern.builder()
                        .source(Collections.singletonList("aws.ecr"))
                        .detailType(Collections.singletonList("ECR Image Action"))
                        .detail(Collections.singletonMap("action-type",
                                Collections.singletonList("PUSH")))
                        .build())
                .build();
        TagUtils.applyTags(rule);
        return rule;
    }
}
