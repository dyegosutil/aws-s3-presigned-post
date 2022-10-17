package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import java.util.Objects;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.META;

public class MetaCondition extends Condition{

    private final String metaName;
    private final String value;
    private final MatchCondition.Operator operator;

    public MetaCondition(MatchCondition.Operator operator, String metaName, String value) {
        super(META);
        Objects.requireNonNull(operator);
        Objects.requireNonNull(metaName);
        Objects.requireNonNull(value);
        this.metaName = metaName;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String[] asAwsPolicyCondition() {
        return new String[]{
                operator.awsOperatorValue,
                super.getConditionField().awsConditionName.concat(metaName),
                value
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaCondition &&
                ((MetaCondition) obj).getConditionField() == this.conditionField &&
                ((MetaCondition) obj).operator == this.operator &&
                Objects.equals(((MetaCondition) obj).metaName, this.metaName);
    }
}
