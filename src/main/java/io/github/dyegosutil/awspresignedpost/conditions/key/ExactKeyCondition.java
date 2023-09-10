package io.github.dyegosutil.awspresignedpost.conditions.key;

import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.EQ;

/** Represents the condition on which it can be chosen which exact value the s3 key must have */
public final class ExactKeyCondition extends KeyCondition {
    public ExactKeyCondition(String expectedExactKeyValue) {
        super(EQ, expectedExactKeyValue);
    }
}
