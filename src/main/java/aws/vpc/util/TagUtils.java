package aws.vpc.util;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awscdk.Tags;
import software.constructs.IConstruct;

public class TagUtils {
    public static void applyTags(IConstruct resource) {
        Map<String, String> tags = createTags();
        tags.forEach((key, value) -> Tags.of(resource).add(key, value));
    }

    private static Map<String, String> createTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("Name", "auto-study");
        return tags;
    }
}
