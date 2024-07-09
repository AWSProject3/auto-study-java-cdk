import aws.vpc.BasicInfraAdminister;
import software.amazon.awscdk.App;

public class AutoCodeApplication {

    public static void main(final String[] args) {
        App app = new App();

        BasicInfraAdminister infraAdminister = new BasicInfraAdminister();
        infraAdminister.createInfra(app, "730335599027", "us-east-2");

        // rds

//        /**
//         *  eks
//         */
//        Stack stack = createStack(app, createEnv("544345130572", "ap-northeast-2"));

        System.out.println("complete");

        app.synth();
    }

//    /**
//     *
//     * eks
//     */
//    private static Stack createStack(App app, Environment env) {
//        return new Stack(app, "EksBlueprintStack", StackProps.builder().env(env).build());
//    }
//
//    private static Environment createEnv(String account, String region) {
//        return Environment.builder()
//                .account(account)
//                .region(region)
//                .build();
//    }

    //action//
}
