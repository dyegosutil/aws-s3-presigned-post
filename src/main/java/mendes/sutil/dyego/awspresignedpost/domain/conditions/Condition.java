package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import mendes.sutil.dyego.awspresignedpost.S3PostSigner;

import java.util.Objects;

/**
 * Contains the common fields for all the conditions
 */
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
        return obj instanceof Condition && ((Condition) obj).getConditionField() == this.conditionField;
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
