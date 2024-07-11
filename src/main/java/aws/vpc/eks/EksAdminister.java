//package aws.vpc.eks;
//
//import aws.vpc.VpcInfraManager;
//import aws.vpc.eks.ecr.EcrRepositoryConfigurator;
//import software.amazon.awscdk.App;
//import software.amazon.awscdk.Environment;
//import software.amazon.awscdk.Stack;
//import software.amazon.awscdk.StackProps;
//
//public class EksAdminister {
//
//    public void createInfra(App app, String account, String region, VpcInfraManager vpcInfraManager) {
//        Environment env = createEnv(account, region);
//        Stack stack = createStack(app, env);
//        EksConfigConfigurator eksConfigConfigurator = new EksConfigConfigurator(stack, vpcInfraManager);
//        eksConfigConfigurator.configure("auto-study-eks");
//
//        EcrRepositoryConfigurator ecrRepositoryConfigurator = new EcrRepositoryConfigurator(stack);
//        ecrRepositoryConfigurator.configure("auto-study-app", 10);
//    }
//
//    private Stack createStack(App app, Environment env) {
//        return new Stack(app, "EksBlueprintStack", StackProps.builder().env(env).build());
//    }
//
//    private Environment createEnv(String account, String region) {
//        return Environment.builder()
//                .account(account)
//                .region(region)
//                .build();
//    }
//}
