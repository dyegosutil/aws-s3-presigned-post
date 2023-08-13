package mendes.sutil.dyego.awspresignedpost.postparams;

/**
 * Represents an AWS S3 Tag
 */
public class Tag {

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private final String key;
    private final String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
