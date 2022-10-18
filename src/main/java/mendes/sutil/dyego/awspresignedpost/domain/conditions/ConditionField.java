package mendes.sutil.dyego.awspresignedpost.domain.conditions;

public enum ConditionField { // TODO move it

    KEY("$key"),
    ALGORITHM("$x-amz-algorithm"),
    CREDENTIAL("$x-amz-credential"),
    SECURITY_TOKEN("$x-amz-security-token"),
    ACL("$acl"),
    CACHE_CONTROL("$Cache-Control"),
    CONTENT_TYPE("$Content-Type"),
    CONTENT_DISPOSITION("$Content-Disposition"),
    CONTENT_ENCODING("$Content-Encoding"),
    EXPIRES("$Expires"),
    SUCCESS_ACTION_REDIRECT("$success_action_redirect"),
    REDIRECT("$redirect"),
    SUCCESS_ACTION_STATUS("$success_action_status"),
    DATE("$x-amz-date"),
    BUCKET("$bucket"),
    TAGGING("$tagging"),
    META("$x-amz-meta-"),
    STORAGE_CLASS("$x-amz-storage-class"),
    WEBSITE_REDIRECT_LOCATION("$x-amz-website-redirect-location"),
    CHECKSUM_CRC32("$x-amz-checksum-crc32"),
    CHECKSUM_SHA256("$x-amz-checksum-sha256"),
    CONTENT_LENGTH_RANGE("content-length-range");
    
    public final String awsConditionName;

    ConditionField(String awsConditionName) {
        this.awsConditionName = awsConditionName;
    }
}