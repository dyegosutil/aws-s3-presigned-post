package io.github.dyegosutil.awspresignedpost.conditions.key;

import io.github.dyegosutil.awspresignedpost.conditions.MatchCondition;

import static io.github.dyegosutil.awspresignedpost.conditions.ConditionField.KEY;

/** Used as a base for classes specifying conditions about how the s3 key should be like */
public abstract class KeyCondition extends MatchCondition {

    protected KeyCondition(Operator operator, String value) {
        super(KEY, operator, value);
    }
}
