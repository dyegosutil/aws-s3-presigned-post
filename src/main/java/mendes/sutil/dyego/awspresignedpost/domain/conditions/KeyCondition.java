package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Used as a base for classes specifying conditions about how the s3 key should be like
 */
public abstract class KeyCondition extends RawCondition{

    public KeyCondition(String value) {
        super(value);
    }
}