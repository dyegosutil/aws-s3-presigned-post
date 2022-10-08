package mendes.sutil.dyego.awspresignedpost.domain.conditions.key;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.KEY;

/**
 * Used as a base for classes specifying conditions about how the s3 key should be like
 */
public abstract class KeyCondition extends MatchCondition {

    public KeyCondition(Operator operator, String bucketValue) {
        super(KEY, operator, bucketValue);
    }
}