package mendes.sutil.dyego.awspresignedpost.domain.conditions.key;

/**
 * Represents the condition on which it can be chosen how the key should start
 */
public class KeyStartingWithCondition extends KeyCondition {

    public KeyStartingWithCondition(String keyStartWithValue) {
        super(Operator.STARTS_WITH, keyStartWithValue);
    }
}