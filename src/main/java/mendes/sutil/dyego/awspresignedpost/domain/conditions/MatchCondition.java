package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Used to specify in which circumstances the upload to s3 should be accepted
 */
public class MatchCondition extends Condition {
    private Match match;
    private String value;

    // TODO use lombok
    public MatchCondition(ConditionField conditionField, Match match, String value) {
        super(conditionField);
        this.match = match;
        this.value = value;
    }

    public ConditionField getConditionField() {
        return conditionField;
    }

    public Match getConditionMatch() {
        return match;
    }

    public void setConditionMatch(Match match) {
        this.match = match;
    }

    public String getValue() {
        return value;
    }

    public enum Match {
        EQ("eq"),
        STARTS_WITH("starts-with");

        private final String value;

        Match(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}