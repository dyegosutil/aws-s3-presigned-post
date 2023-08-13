package mendes.sutil.dyego.awspresignedpost.conditions;

import java.util.Objects;

import static mendes.sutil.dyego.awspresignedpost.conditions.ConditionField.META;

public class MetaCondition extends MatchCondition {

  private final String metaName;

  public MetaCondition(Operator operator, String metaName, String value) {
    super(META, operator, value);
    Objects.requireNonNull(operator);
    Objects.requireNonNull(metaName);
    Objects.requireNonNull(value);
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
