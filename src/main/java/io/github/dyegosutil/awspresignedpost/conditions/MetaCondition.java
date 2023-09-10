package io.github.dyegosutil.awspresignedpost.conditions;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MetaCondition extends MatchCondition {

    private final String metaName;

    public MetaCondition(Operator operator, String metaName, String value) {
        super(ConditionField.META, operator, value);
        requireNonNull(operator);
        requireNonNull(metaName);
        requireNonNull(value);
        this.metaName = metaName;
    }

    @Override
    public String[] asAwsPolicyCondition() {
        return new String[] {
            getConditionOperator().awsOperatorValue,
            super.getConditionField().valueForAwsPolicy.concat(metaName),
            getValue()
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj instanceof MetaCondition
                && ((MetaCondition) obj).getConditionField() == this.conditionField
                && Objects.equals(((MetaCondition) obj).metaName, this.metaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), metaName);
    }

    public String getMetaName() {
        return metaName;
    }
}
