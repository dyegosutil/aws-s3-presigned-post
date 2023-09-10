package io.github.dyegosutil.awspresignedpost.conditions.key;

import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.STARTS_WITH;

/** Represents the condition on which it can be chosen how the key should start */
public final class KeyStartingWithCondition extends KeyCondition {

    public KeyStartingWithCondition(String keyStartWithValue) {
        super(STARTS_WITH, keyStartWithValue);
    }
}
