package mendes.sutil.dyego.awspresignedpost.conditions;

public class ChecksumCondition extends MatchCondition {

  public ChecksumCondition(ConditionField conditionField, String value) {
    super(conditionField, Operator.EQ, value);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    return o instanceof ChecksumCondition;
  }
}
