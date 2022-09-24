package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Used as a base for classes specifying conditions about how the s3 key should be like
 */
public abstract class KeyCondition {

    /**
     * Value to be used while building the conditions.
     */
    private final String value;

    public KeyCondition(String value) {
        this.value = value;
    }

    /**
     * @return The value to be used while building the conditions.
     */
    public String getValue() {
        return this.value;
    }
}