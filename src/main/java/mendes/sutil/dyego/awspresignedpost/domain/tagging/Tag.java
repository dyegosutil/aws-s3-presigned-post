package mendes.sutil.dyego.awspresignedpost.domain.tagging;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * Represents an AWS S3 Tag
 */
public class Tag {

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @SuppressWarnings("unused") // Used by JAXB
    @XmlElement(name = "Key")
    private final String key;

    @SuppressWarnings("unused") // Used by JAXB
    @XmlElement(name = "Value")
    private final String value;
}
