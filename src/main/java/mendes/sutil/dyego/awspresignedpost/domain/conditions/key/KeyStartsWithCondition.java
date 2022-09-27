package mendes.sutil.dyego.awspresignedpost.domain.conditions.key;

/**
 * Represents the condition on which it can be chosen how the key should start
 */
public class KeyStartsWithCondition extends KeyCondition {

    public KeyStartsWithCondition(String keyStartWithValue) {
        super(ConditionMatch.STARTS_WITH, keyStartWithValue);
    }
}