package io.github.dyegosutil.awspresignedpost.conditions;

import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.EQ;

public class ChecksumCondition extends MatchCondition {

    public ChecksumCondition(ConditionField conditionField, String value) {
        super(conditionField, EQ, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        return o instanceof ChecksumCondition;
    }
}
