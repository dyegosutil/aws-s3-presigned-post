package mendes.sutil.dyego.awspresignedpost.domain.conditions;

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
}
