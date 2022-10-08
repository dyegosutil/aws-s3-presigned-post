package mendes.sutil.dyego.awspresignedpost.domain.conditions;

public enum ConditionField { // TODO move it

    KEY("$key"),
    SUCCESS_ACTION_STATUS("$success_action_status"), // TODO Confirm if this is correct.
    ALGORITHM("$x-amz-algorithm"),
    CREDENTIAL("$x-amz-credential"),
    CONTENT_TYPE(""),
    CONTENT_ENCODING(""),
    CONTENT_DISPOSITION(""),
    SUCCESS_ACTION_REDIRECT(""),
    ACL(""),
    DATE("$x-amz-date"), // confirm all these fields to see which condition matching they accept.
    BUCKET("$bucket"),
    CONTENT_LENGTH_RANGE("content-length-range");
    
    public final String awsConditionName;

    ConditionField(String awsConditionName) {
        this.awsConditionName = awsConditionName;
    }
}