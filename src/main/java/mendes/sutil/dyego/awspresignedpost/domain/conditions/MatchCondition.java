package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Used to specify in which circumstances the upload to s3 should be accepted
 */
public class MatchCondition extends Condition {
    private final Operator operator;
    private final String value;

    // TODO use lombok
    public MatchCondition(ConditionField conditionField, Operator operator, String value) {
        super(conditionField);
        this.operator = operator;
        this.value = value;
    }

    public ConditionField getConditionField() {
        return conditionField;
    }

    public Operator getConditionMatch() {
        return operator;
    }

    public String getValue() {
        return value;
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