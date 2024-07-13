package aws.s3;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class S3Administer {

    public void createInfra(App app, String account, String region) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        S3BucketConfigurator bucketConfigurator = new S3BucketConfigurator(stack);
        bucketConfigurator.configureBucket("auto-study-bucket-ver3", 100, 30, true, false);
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "S3Stack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
