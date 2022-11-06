package mendes.sutil.dyego.awspresignedpost;

import jakarta.xml.bind.annotation.XmlElement;

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
