package mendes.sutil.dyego.awspresignedpost.conditions;

public enum ConditionField {

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
    SUCCESS_ACTION_STATUS("$success_action_status"),
    DATE("$x-amz-date"),
    BUCKET("$bucket"),
    TAGGING("$tagging"),
    META("$x-amz-meta-"),
    STORAGE_CLASS("$x-amz-storage-class"),
    WEBSITE_REDIRECT_LOCATION("$x-amz-website-redirect-location"),
    CHECKSUM_CRC32("$x-amz-checksum-crc32"),
    CHECKSUM_CRC32C("$x-amz-checksum-crc32c"),
    CHECKSUM_SHA256("$x-amz-checksum-sha256"),
    CHECKSUM_SHA1("$x-amz-checksum-sha1"),
    SERVER_SIDE_ENCRYPTION("$x-amz-server-side-encryption"),
    SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID("$x-amz-server-side-encryption-aws-kms-key-id"),
    SERVER_SIDE_ENCRYPTION_CONTEXT("$x-amz-server-side-encryption-context"),
    SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED("$x-amz-server-side-encryption-bucket-key-enabled"),
    SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM("$x-amz-server-side-encryption-customer-algorithm"),
    SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY("$x-amz-server-side-encryption-customer-key"),
    SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5("$x-amz-server-side-encryption-customer-key-MD5"),
    CONTENT_LENGTH_RANGE("content-length-range");

    /**
     * Generally starting with '$', indicates a condition to be fulfilled for the upload to be successful. This value
     * is placed in the policy and sent to S3 AWS in the 'policy' field
     */
    public final String valueForAwsPolicy;

    /**
     * Does not start with '$', indicates a condition to be fulfilled for the upload to be successful. This value
     * is the one to be used as parameters in the client used to perform the POST to S3 AWS
     */
    public final String valueForApiCall;

    ConditionField(String valueForAwsPolicy) {
        this.valueForAwsPolicy = valueForAwsPolicy;
        this.valueForApiCall = valueForAwsPolicy.replace("$","");
    }
}