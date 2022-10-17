package mendes.sutil.dyego.awspresignedpost.domain.tagging;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Set;

/**
 * Represents an AWS S3 Tagging xml structure
 */
@XmlRootElement(name = "Tagging")
public class Tagging {

    @SuppressWarnings("unused") // Used by JAXB
    public Tagging() {}

    public Tagging(Set<Tag> tagSet) {
        this.tagSet = tagSet;
    }

    @SuppressWarnings("unused") // Used by JAXB
    @XmlElementWrapper(name = "TagSet")
    @XmlElement(name = "Tag")
    private Set<Tag> tagSet;

    /**
     * @return The xml representation of the tagging in the following format:
     * <pre> {@code
     * <Tagging>
     *     <TagSet>
     *         <Tag>
     *             <Key>MyTagName</Key>
     *             <Value>MyTagValue</Value>
     *         </Tag>
     *     </TagSet>
     * </Tagging>
     * }
     * </pre>
     */
    public String toXml() {
        return TaggingHelper.marshal(this);
    }
}