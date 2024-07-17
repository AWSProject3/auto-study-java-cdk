package aws.ecr;

import aws.vpc.util.TagUtils;
import java.util.List;
import software.amazon.awscdk.services.ecr.LifecycleRule;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.Repository.Builder;
import software.amazon.awscdk.services.ecr.TagStatus;
import software.constructs.Construct;

public class EcrRepositoryConfigurator {
    private final Construct scope;

    public EcrRepositoryConfigurator(Construct scope) {
        this.scope = scope;
    }

    public void configure(String ecrRepoName, int maxImageCount) {
        Repository repository = Builder.create(scope, ecrRepoName)
                .repositoryName(ecrRepoName)
                .imageScanOnPush(true)
                .lifecycleRules(createLifeCycleRules(maxImageCount))
                .build();
        TagUtils.applyTags(repository);
    }

    private List<LifecycleRule> createLifeCycleRules(int maxImageCount) {
        return List.of(
                LifecycleRule.builder()
                        .maxImageCount(maxImageCount)
                        .tagStatus(TagStatus.ANY)
                        .build()
        );
    }
}
