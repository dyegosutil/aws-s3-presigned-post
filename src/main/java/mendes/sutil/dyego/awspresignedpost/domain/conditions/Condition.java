package mendes.sutil.dyego.awspresignedpost.domain.conditions;

/**
 * Uses to specify in which circumstances the upload to s3 should be accepted
 */
public class Condition {
    private ConditionField conditionField;
    private ConditionMatch conditionMatch;
    private String value;

    // TODO use lombok
    public Condition(ConditionField conditionField, ConditionMatch conditionMatch, String value) {
        this.conditionField = conditionField;
        this.conditionMatch = conditionMatch;
        this.value = value;
    }

    public ConditionField getConditionField() {
        return conditionField;
    }

    public void setConditionField(ConditionField conditionField) {
        this.conditionField = conditionField;
    }

    public ConditionMatch getConditionMatch() {
        return conditionMatch;
    }

    public void setConditionMatch(ConditionMatch conditionMatch) {
        this.conditionMatch = conditionMatch;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum ConditionField { // TODO move it
        KEY("$key"), // TODO confirm if the fields should have $ or not depending on the condition.
        SUCCESS_ACTION_STATUS("$success_action_status"), // TODO Confirm if this is correct.
        ALGORITHM("$x-amz-algorithm"),
        CREDENTIAL("$x-amz-credential"),
        CONTENT_TYPE(""),
        CONTENT_ENCODING(""),
        CONTENT_DISPOSITION(""),
        SUCCESS_ACTION_REDIRECT(""),
        ACL(""),
        DATE("$x-amz-date"), // confirm all these fields to see which condition matching they accept.
        BUCKET("");
        public final String name;

        ConditionField(String name) {
            this.name = name;
        }
    }

    public enum ConditionMatch{
        EQ("eq"),
        STARTS_WITH("starts-with");

        private final String value;

        ConditionMatch(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}