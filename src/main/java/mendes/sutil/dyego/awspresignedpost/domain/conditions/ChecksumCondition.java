package mendes.sutil.dyego.awspresignedpost.domain.conditions;

public class ChecksumCondition extends MatchCondition {

    public ChecksumCondition(ConditionField conditionField, String value) {
        super(conditionField, Operator.EQ, value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChecksumCondition;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
