package aws.s3;

import aws.vpc.util.TagUtils;
import java.util.List;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.Bucket.Builder;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.s3.NoncurrentVersionTransition;
import software.amazon.awscdk.services.s3.StorageClass;
import software.amazon.awscdk.services.s3.Transition;
import software.constructs.Construct;

public class S3BucketConfigurator {
    private final Construct scope;

    public S3BucketConfigurator(Construct scope) {
        this.scope = scope;
    }

    public void configureBucket(String bucketName, int expirationInDays, int nonCurrentVersionTransitionDays,
                                boolean enableVersioning, boolean enablePublicAccess) {
        Bucket bucket = Builder.create(scope, bucketName)
                .bucketName(bucketName)
                .encryption(BucketEncryption.S3_MANAGED)
                .versioned(enableVersioning)
                .blockPublicAccess(getBlockPublicAccess(enablePublicAccess))
                .lifecycleRules(createLifecycleRules(expirationInDays, nonCurrentVersionTransitionDays))
                .build();
        TagUtils.applyTags(bucket);
    }

    private BlockPublicAccess getBlockPublicAccess(boolean enablePublicAccess) {
        return enablePublicAccess ? BlockPublicAccess.BLOCK_ACLS : BlockPublicAccess.BLOCK_ALL;
    }

    private List<LifecycleRule> createLifecycleRules(int expirationInDays, int nonCurrentVersionTransitionDays) {
        return List.of(
                LifecycleRule.builder()
                        .expiration(Duration.days(expirationInDays))
                        .noncurrentVersionTransitions(
                                createNonCurrentVersionTransitions(nonCurrentVersionTransitionDays))
                        .transitions(createTransitions())
                        .build()
        );
    }

    private List<NoncurrentVersionTransition> createNonCurrentVersionTransitions(int nonCurrentVersionTransitionDays) {
        return List.of(
                NoncurrentVersionTransition.builder()
                        .transitionAfter(Duration.days(nonCurrentVersionTransitionDays))
                        .storageClass(StorageClass.INFREQUENT_ACCESS)
                        .build()
        );
    }

    private List<Transition> createTransitions() {
        return List.of(
                Transition.builder()
                        .transitionAfter(Duration.days(30))
                        .storageClass(StorageClass.INFREQUENT_ACCESS)
                        .build(),
                Transition.builder()
                        .transitionAfter(Duration.days(60))
                        .storageClass(StorageClass.GLACIER)
                        .build()
        );
    }
}
