package io.github.dyegosutil.awspresignedpost.presigned;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class PreSignedPost {
    private final String url;

    private final Map<String, String> conditions;

    public PreSignedPost(String url, Map<String, String> conditions) {
        requireNonNull(url);
        requireNonNull(conditions);
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
