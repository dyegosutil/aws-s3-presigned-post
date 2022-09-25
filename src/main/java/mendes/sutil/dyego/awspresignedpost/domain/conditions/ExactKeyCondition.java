package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import mendes.sutil.dyego.awspresignedpost.domain.Condition;

import java.util.List;

import static mendes.sutil.dyego.awspresignedpost.domain.Condition.ConditionMatch.EQ;

/**
 * Represents the condition on which it can be chosen which exact value the s3 key must have
 */
public class ExactKeyCondition extends KeyCondition{
    public ExactKeyCondition(String expectedExactKeyValue) {
        super(expectedExactKeyValue);
    }

    @Override
    public void addItselfTo(List<Condition> conditions) {
        conditions.add(
                new Condition(Condition.ConditionField.KEY, EQ, super.getValue())
        );
    }
}