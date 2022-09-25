package mendes.sutil.dyego.awspresignedpost.domain.conditions.key;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition.ConditionField.KEY;

/**
 * Used as a base for classes specifying conditions about how the s3 key should be like
 */
public abstract class KeyCondition extends Condition {

    public KeyCondition(ConditionMatch conditionMatch, String bucketValue) {
        super(KEY, conditionMatch, bucketValue);
    }
}