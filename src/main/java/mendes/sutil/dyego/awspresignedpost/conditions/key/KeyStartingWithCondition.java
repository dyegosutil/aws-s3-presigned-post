package mendes.sutil.dyego.awspresignedpost.conditions.key;

import mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition;

/**
 * Represents the condition on which it can be chosen how the key should start
 */
public class KeyStartingWithCondition extends KeyCondition {

    public KeyStartingWithCondition(String keyStartWithValue) {
        super(MatchCondition.Operator.STARTS_WITH, keyStartWithValue);
    }
}