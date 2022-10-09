package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import java.util.Objects;

/**
 * Contains the common fields for all the conditions
 */
public class Condition {

    protected final ConditionField conditionField;

    Condition(ConditionField conditionField) {
        this.conditionField = conditionField;
    }

    public ConditionField getConditionField() {
        return conditionField;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Condition && ((Condition) obj).getConditionField() == this.conditionField;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionField);
    }
}
