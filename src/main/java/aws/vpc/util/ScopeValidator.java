package aws.vpc.util;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.Vpc;

public class ScopeValidator {

    public static Vpc extractVpcBy(Construct scope) {
        if (validateScope(scope)) {
            return (Vpc) scope;
        }
        throw new RuntimeException("scope is not vpc");
    }

    private static boolean validateScope(Construct scope) {
        return scope instanceof Vpc;
    }
}
