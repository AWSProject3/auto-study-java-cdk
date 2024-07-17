package aws.vpc.elasticcache;

import aws.vpc.VpcInfraManager;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class RedisAdminister {

    public void createInfra(App app, String account, String region, VpcInfraManager vpcInfraManager) {
        Environment env = createEnv(account, region);
        Stack stack = createStack(app, env);
        RedisConfigurator redisConfigurator = new RedisConfigurator(stack, vpcInfraManager,
                new RedisSubnetGroup(stack), new RedisSecurityGroup(stack));
        redisConfigurator.configure("redis-cluster", "RedisCluster", 2, "6.x");
    }

    private Stack createStack(App app, Environment env) {
        return new Stack(app, "RedisStack", StackProps.builder().env(env).build());
    }

    private Environment createEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
