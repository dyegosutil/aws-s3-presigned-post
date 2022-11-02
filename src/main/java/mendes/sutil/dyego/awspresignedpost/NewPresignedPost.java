package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;


// TODO say that it will return the ${filename} for key or change it to not return the ${filename}
public class NewPresignedPost {
    private final String url;

    private final Map<String, String> conditions;

    NewPresignedPost(String url, Map<String, String> conditions) {
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
