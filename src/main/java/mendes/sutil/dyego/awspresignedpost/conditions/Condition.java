package mendes.sutil.dyego.awspresignedpost.conditions;

import java.util.Objects;

public abstract class Condition {

  protected final ConditionField conditionField;

  Condition(ConditionField conditionField) {
    Objects.requireNonNull(conditionField);
    this.conditionField = conditionField;
  }

  public ConditionField getConditionField() {
    return conditionField;
  }

  /**
   * Generates this condition's correspondent String array to be added to the policy
   *
   * @return
   */
  public abstract String[] asAwsPolicyCondition();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Condition condition = (Condition) o;
    return conditionField == condition.conditionField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionField);
  }
}
