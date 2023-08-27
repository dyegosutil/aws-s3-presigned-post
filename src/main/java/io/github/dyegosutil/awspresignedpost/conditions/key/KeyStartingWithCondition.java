package io.github.dyegosutil.awspresignedpost.conditions.key;

import io.github.dyegosutil.awspresignedpost.conditions.MatchCondition;

/** Represents the condition on which it can be chosen how the key should start */
public final class KeyStartingWithCondition extends KeyCondition {

    public KeyStartingWithCondition(String keyStartWithValue) {
        super(MatchCondition.Operator.STARTS_WITH, keyStartWithValue);
    }
}
