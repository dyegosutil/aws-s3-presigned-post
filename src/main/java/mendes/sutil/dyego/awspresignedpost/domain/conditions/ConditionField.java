package mendes.sutil.dyego.awspresignedpost.domain.conditions;

public enum ConditionField { // TODO move it

    KEY("$key"),
    SUCCESS_ACTION_STATUS("$success_action_status"), // TODO Confirm if this is correct.
    ALGORITHM("$x-amz-algorithm"),
    CREDENTIAL("$x-amz-credential"),
    SUCCESS_ACTION_REDIRECT(""),
    ACL(""),
    CACHE_CONTROL("$Cache-Control"),
    CONTENT_TYPE("$Content-Type"),
    CONTENT_DISPOSITION("$Content-Disposition"),
    CONTENT_ENCODING("$Content-Encoding"),
    DATE("$x-amz-date"),
    BUCKET("$bucket"),
    CONTENT_LENGTH_RANGE("content-length-range");
    
    public final String awsConditionName;

    ConditionField(String awsConditionName) {
        this.awsConditionName = awsConditionName;
    }
}