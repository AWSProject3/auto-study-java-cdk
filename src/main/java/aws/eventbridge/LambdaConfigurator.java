package aws.eventbridge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.BundlingOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Function.Builder;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

public class LambdaConfigurator {

    private static final String LAMBDA_HANDLER = "update_gitops_repo.lambda_handler";

    private final Construct scope;

    public LambdaConfigurator(Construct scope) {
        this.scope = scope;
    }

    public Function createLambdaWithExecutionRole() {
        Function lambda = createLambda();
        lambda.addToRolePolicy(createLambdaExecutionRole());
        return lambda;
    }

    private Function createLambda() {
        BundlingOptions bundling = BundlingOptions.builder()
                .command(createPackagingInstructions())
                .image(Runtime.PYTHON_3_11.getBundlingImage())
                .user("root")
                .outputType(BundlingOutput.NOT_ARCHIVED)
                .build();

        return Builder.create(scope, "UpdateGitOpsRepo")
                .runtime(Runtime.PYTHON_3_11)
                .handler(LAMBDA_HANDLER)
                .code(Code.fromAsset("lambda", AssetOptions.builder()
                        .bundling(bundling)
                        .build()))
                .timeout(Duration.seconds(30))
                .memorySize(256)
                .build();
    }

    private List<String> createPackagingInstructions() {
        return Arrays.asList(
                "/bin/sh", "-c",
                "pip install -r requirements.txt -t /asset-output && cp update_gitops_repo.py /asset-output"
        );
    }

    private PolicyStatement createLambdaExecutionRole() {
        return PolicyStatement.Builder.create()
                .actions(Collections.singletonList("ecr:DescribeImages"))
                .resources(Collections.singletonList("*"))
                .build();
    }
}
