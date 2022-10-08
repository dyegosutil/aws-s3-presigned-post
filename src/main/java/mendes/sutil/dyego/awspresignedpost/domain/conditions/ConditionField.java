package mendes.sutil.dyego.awspresignedpost.domain.conditions;

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
    BUCKET(""),
    CONTENT_LENGTH_RANGE("content-length-range"); // if problems happens it might be due to missing $
    public final String name;

    ConditionField(String name) {
        this.name = name;
    }
}