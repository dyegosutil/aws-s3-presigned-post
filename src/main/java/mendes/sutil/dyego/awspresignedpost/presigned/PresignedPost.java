package mendes.sutil.dyego.awspresignedpost.presigned;

import java.util.Map;
import java.util.Objects;

public class PresignedPost {
    private final String url;

    private final Map<String, String> conditions;

    public PresignedPost(String url, Map<String, String> conditions) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(conditions);
        this.url = url;
        this.conditions = conditions;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }
}
