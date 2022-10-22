package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptySet;

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
    CHECKSUM_CRC32C("$x-amz-checksum-crc32c"),
    CHECKSUM_SHA256("$x-amz-checksum-sha256"),
    CHECKSUM_SHA1("$x-amz-checksum-sha1"),
    SERVER_SIDE_ENCRYPTION("$x-amz-server-side-encryption"),
    SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID(
            "$x-amz-server-side-encryption-aws-kms-key-id",
            SERVER_SIDE_ENCRYPTION
    ),
    SERVER_SIDE_ENCRYPTION_CONTEXT(
            "$x-amz-server-side-encryption-context",
            SERVER_SIDE_ENCRYPTION
    ),
    SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED(
            "$x-amz-server-side-encryption-bucket-key-enabled",
            SERVER_SIDE_ENCRYPTION
    ),
    CONTENT_LENGTH_RANGE("content-length-range");
    
    public final String awsConditionName;
    public final Set<ConditionField> requiredConditionFields;

    ConditionField(String awsConditionName) {
        this.awsConditionName = awsConditionName;
        this.requiredConditionFields = emptySet();
    }

    /**
     * @param requiredConditionField The {@link ConditionField} which must also be present in the policy for the upload
     *                               to be successful.
     */
    ConditionField(String awsConditionName, ConditionField requiredConditionField) {
        this.awsConditionName = awsConditionName;
        this.requiredConditionFields = Collections.singleton(requiredConditionField);
    }
}