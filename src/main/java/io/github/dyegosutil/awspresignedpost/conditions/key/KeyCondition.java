package io.github.dyegosutil.awspresignedpost.conditions.key;

import io.github.dyegosutil.awspresignedpost.conditions.ConditionField;
import io.github.dyegosutil.awspresignedpost.conditions.MatchCondition;

/** Used as a base for classes specifying conditions about how the s3 key should be like */
public abstract class KeyCondition extends MatchCondition {

    protected KeyCondition(Operator operator, String value) {
        super(ConditionField.KEY, operator, value);
    }
}
