package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import java.util.Objects;

public abstract class Condition {

    protected final ConditionField conditionField;

    Condition(ConditionField conditionField) {
        this.conditionField = conditionField;
    }

    public ConditionField getConditionField() {
        return conditionField;
    } // TODO protected?

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MatchCondition && ((MatchCondition) obj).getConditionField() == this.conditionField;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionField);
    }

    /**
     * Generates this condition's correspondent String array to be added to the policy
     * @return
     */
    public abstract String[] asAwsPolicyCondition();

}
