package mendes.sutil.dyego.awspresignedpost.conditions;

public class MatchCondition extends Condition {
  private final Operator operator;
  private final String value;

  public MatchCondition(ConditionField conditionField, Operator operator, String value) {
    super(conditionField);
    this.operator = operator;
    this.value = value;
  }

  public Operator getConditionOperator() {
    return operator;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String[] asAwsPolicyCondition() {
    return new String[] {
      getConditionOperator().awsOperatorValue, conditionField.valueForAwsPolicy, getValue()
    };
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    return o instanceof MatchCondition
        && ((MatchCondition) o).getConditionField() == this.conditionField;
  }

  public enum Operator {
    EQ("eq"),
    STARTS_WITH("starts-with");

    public final String awsOperatorValue;

    Operator(String awsOperatorValue) {
      this.awsOperatorValue = awsOperatorValue;
    }

    @Override
    public String toString() {
      return this.awsOperatorValue;
    }
  }
}
