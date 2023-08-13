package mendes.sutil.dyego.awspresignedpost.conditions.key;

import mendes.sutil.dyego.awspresignedpost.conditions.ConditionField;
import mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition;

/** Used as a base for classes specifying conditions about how the s3 key should be like */
public abstract class KeyCondition extends MatchCondition {

  public KeyCondition(Operator operator, String value) {
    super(ConditionField.KEY, operator, value);
  }
}
