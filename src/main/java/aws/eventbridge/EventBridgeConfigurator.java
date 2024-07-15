package aws.eventbridge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.BundlingOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

public class EventBridgeConfigurator {

    private static final String LAMBDA_HANDLER = "update_gitops_repo.lambda_handler";

    private final Construct scope;

    public EventBridgeConfigurator(Construct scope) {
        this.scope = scope;
    }

    public void configure() {
        Function lambda = createLambda();
        lambda.addToRolePolicy(createLambdaExecutionRole());

        Rule eventBridgeRule = createEventBridgeRule();
        eventBridgeRule.addTarget(new LambdaFunction(lambda));
    }

    private PolicyStatement createLambdaExecutionRole() {
        return PolicyStatement.Builder.create()
                .actions(Collections.singletonList("ecr:DescribeImages"))
                .resources(Collections.singletonList("*"))
                .build();
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

    private Function createLambda() {
        List<String> packagingInstructions = Arrays.asList(
                "/bin/sh", "-c",
                "pip install -r requirements.txt -t /asset-output && cp update_gitops_repo.py /asset-output"
        );

        BundlingOptions bundling = BundlingOptions.builder()
                .command(packagingInstructions)
                .image(Runtime.PYTHON_3_11.getBundlingImage())
                .user("root")
                .outputType(BundlingOutput.ARCHIVED)
                .build();

        return Function.Builder.create(scope, "UpdateGitOpsRepo")
                .runtime(Runtime.PYTHON_3_11)
                .handler(LAMBDA_HANDLER)
                .code(Code.fromAsset("lambda", AssetOptions.builder()
                        .bundling(bundling)
                        .build()))
                .timeout(Duration.seconds(30))
                .memorySize(256)
                .build();
    }
}
