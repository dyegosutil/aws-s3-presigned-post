package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Represents the condition on which it can be chosen which exact value the s3 key must have
 */
public class ExactKeyCondition extends KeyCondition{
    public ExactKeyCondition(String expectedExactKeyValue) {
        super(expectedExactKeyValue);
    }
}