package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import mendes.sutil.dyego.awspresignedpost.domain.AmzExpirationDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.*;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.tagging.Tag;
import mendes.sutil.dyego.awspresignedpost.domain.tagging.Tagging;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.*;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.*;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator.EQ;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator.STARTS_WITH;

/**
 * A pre-signed POST request that can be executed at a later time without requiring additional signing or
 * authentication.
 */
// TODO should it really be final?
@Getter
public final class PostParams {

    // TODO which fields are alwyas going to be there?
    // TODO Double check if key is really mandatory, if it goes in the signature. If the file can be uploaded without
    //  having key in the signature
    private final String bucket;
    private final Region region;

    private final AmzExpirationDate amzExpirationDate;
    private final Map<ConditionField, Condition> conditions;

    private PostParams(
            String bucket,
            Region region,
            AmzExpirationDate amzExpirationDate,
            Map<ConditionField, Condition> conditions
    ){
        Objects.requireNonNull(bucket);
        Objects.requireNonNull(region);
        Objects.requireNonNull(amzExpirationDate);
        Objects.requireNonNull(conditions);
        this.amzExpirationDate = amzExpirationDate;
        this.bucket = bucket;
        this.region = region;
        this.conditions = conditions;
    }

    /**
     * Accepts all the minimum necessary parameters to generate a pre-signed valid pre-signed POST.
     * // TODO add additional information because this method is too cool
     *
     * @param region Region to be used in the signature
     * @param expirationDate Date until when the pre-signed post can be used.
     * @param keyCondition TODO You can use the ConditionHelper to provide the values
     * @param bucket The bucket when the file should be uploaded to.
     * @return A PostParams builder which allows more fine-grained conditions to be added
     */
    public static Builder builder(
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyCondition keyCondition
    ){
        // TODO Enforce UTC expirationDate? Test what happens if this is expired in another timezone and the lib creates the pre-signed in UTC
        ;
        return new Builder(region, new AmzExpirationDate(expirationDate), keyCondition, bucket);
    }

    public static final class Builder {
        private final Map<ConditionField, Condition> conditions = new HashMap<>();
        private final Set<Tag> tags = new HashSet<>();
        private final String bucket;
        private final Region region;
        private final AmzExpirationDate amzExpirationDate;
        private final Map<ConditionField,TreeSet<ConditionField>> dependentConditionFields;

        {
            dependentConditionFields = new HashMap<>();
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION))
            );
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_CONTEXT,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION))
            );
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION))
            );
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5))
            );
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5))
            );
            dependentConditionFields.put(
                    SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5,
                    new TreeSet<>(List.of(SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY))
            );
        }

        private Builder(Region region, AmzExpirationDate amzExpirationDate, KeyCondition keyCondition, String bucket) {
            // TODO add validation for expiration date?
            this.region = region;
            this.amzExpirationDate = amzExpirationDate;
            this.conditions.put(KEY, keyCondition);
            this.conditions.put(BUCKET, new MatchCondition(BUCKET, EQ, bucket));
            this.bucket = bucket;
        }

        public PostParams build(){
            validateConditions();
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            addTags();
            return new PostParams(
                    bucket,
                    region,
                    amzExpirationDate,
                    conditions
            );
        }

        private void validateConditions() {
            conditions.keySet().forEach(
                    conditionField -> {
                        Set<ConditionField> requiredConditions = dependentConditionFields.get(conditionField);
                        if(requiredConditions != null) {
                            requiredConditions.forEach(requiredConditionField -> {
                                if (!conditions.containsKey(requiredConditionField)) {
                                    throw new IllegalArgumentException(
                                            String.format(
                                                    "The condition %s requires the condition(s) %s to be present",
                                                    conditionField,
                                                    requiredConditions
                                            )
                                    );
                                }
                            });
                        }
                    }
            );
        }

        /**
         * Add tags if the method {@link Builder#withTag(String, String)} was used
         */
        private void addTags() {
            if(!tags.isEmpty()) {
                String taggingXml = new Tagging(tags).toXml();
                withTaggingCondition(taggingXml);
            }
        }

        /**
         * Makes sure that an exact ({@link MatchCondition.Operator#EQ}) mutually exclusive condition is not added with
         * a startsWith ({@link MatchCondition.Operator#STARTS_WITH}) condition.
         *
         * @param conditionField Condition type specified
         * @param value          Specifiled value for this condition
         * @return @return The {@link Builder} object
         */
        private Builder withCondition(ConditionField conditionField, String value) {
            return assertUniquenessAndAdd(new MatchCondition(conditionField, EQ, value));
        }

        private Builder withTaggingCondition(String value) {
            this.conditions.put(TAGGING, new MatchCondition(TAGGING, EQ, value));
            return this;
        }

        /**
         * Makes sure that a startsWith ({@link MatchCondition.Operator#STARTS_WITH})mutually exclusive condition is not added with
         * an exact ({@link MatchCondition.Operator#EQ}) condition.
         *
         * @param conditionField Condition type specified
         * @param value          Specifiled value for this condition
         * @return @return The {@link Builder} object
         */
        private Builder withStartingWithCondition(ConditionField conditionField, String value) {
            return assertUniquenessAndAdd(new MatchCondition(conditionField, STARTS_WITH, value));
        }

        private Builder assertUniquenessAndAdd(MatchCondition matchCondition) {
            return addIfUnique(matchCondition, getInvalidConditionExceptionMessage(matchCondition.getConditionField()));
        }

        private Builder assertUniquenessAndAdd(ChecksumCondition checksumCondition) {
            new HashSet<>(Arrays.asList(CHECKSUM_CRC32, CHECKSUM_CRC32C, CHECKSUM_SHA1, CHECKSUM_SHA256)).forEach(a -> {
                if(conditions.containsKey(a)) {
                    throw new IllegalArgumentException("Only one checksum condition CRC32, CRC32C, SHA1 or SHA256 can be added at the same time");
                }
            });
            conditions.put(checksumCondition.getConditionField(), checksumCondition);
            return this;
        }

        private Builder addIfUnique(Condition condition, String errorMessage) {
            if (conditions.containsKey(condition.getConditionField())){
                throw new IllegalArgumentException(errorMessage);
            }
            this.conditions.put(condition.getConditionField(), condition);
            return this;
        }

        private void throwExceptionIfConditionIsPresent(
                ConditionField conditionField,
                String errorMessage
        ) {
            if (conditions.containsKey(conditionField)){
                throw new IllegalArgumentException(errorMessage);
            }
        }

        private Builder assertUniquenessAndAddTagging(String value) {
            if (!tags.isEmpty()) {
                throw new IllegalArgumentException("Either the method withTag() or withTagging() can be used for adding tagging, not both");
            }
            return withTaggingCondition(value);
        }

        private Builder assertUniquenessAndAddTag(String key, String value) {
            throwExceptionIfConditionIsPresent(
                    TAGGING,
                    "Either the method withTag() or withTagging() can be used for adding tagging, not both"
            );
            tags.add(new Tag(key, value));
            return this;
        }

        private String getInvalidConditionExceptionMessage(ConditionField conditionField) {
            return String.format("Only one %s condition can be used", conditionField.name());
        }

        /**
         * Used to limit the size of the file to be uploaded
         * <p>
         * For example, calling the method with the values withContentLengthRange(1048576, 10485760) allows the
         * upload of a file from 1 to 10 MiB
         *
         * @param minimumValue Specified in bytes, indicates the minimum size of the file for it to be accepted
         * @param maximumValue Specified in bytes, indicates the maximum size of the file for it to be accepted
         * @return The {@link Builder} object
         */
        public Builder withContentLengthRange(long minimumValue, long maximumValue) {
            ContentLengthRangeCondition condition = new ContentLengthRangeCondition(minimumValue, maximumValue);
            this.conditions.put(condition.getConditionField(), condition);
            return this;
        }

        // TODO Rename AWS mentions in the lib to Amazon S3
        /**
         * Used to define how should be the response code returned by AWS when the upload is successful. The enum
         * received as parameter here will be converted to 200, 201 or 204. Same values that should be used while using
         * the pre-singed post to upload the file.
         * <p>
         * If the value is set to OK (200) or NO_CONTENT (204), Amazon S3 returns an empty document with a 200 or 204 status code.
         * <p>
         * If the value is set to CREATED (201), Amazon S3 returns an XML document with a 201 status code:
         * <pre> {@code
         * <?xml version="1.0" encoding="UTF-8"?>
         * <PostResponse>
         *     <Location>https://mybucket.s3.eu-central-1.amazonaws.com/myfile.txt</Location>
         *     <Bucket>mybucket</Bucket>
         *     <Key>myfile.txt</Key>
         *     <ETag>"d41d8cd98f00b204e9800998ecf8427f"</ETag>
         * </PostResponse>
         * }
         * </pre>
         * <p>
         * If you don't specify {@link PostParams.Builder#withSuccessActionRedirect(String)} or
         * {@link PostParams.Builder#withSuccessActionRedirectStartingWith(String)}, the status code 204 is returned by
         * default to the client when the upload succeeds.
         * @param successActionStatus Http code to be returned when the upload is successful
         * @return The {@link Builder} object
         */
        public Builder withSuccessActionStatus(SuccessActionStatus successActionStatus) {
            return withCondition(SUCCESS_ACTION_STATUS, successActionStatus.getCode());
        }

        public enum EncryptionAlgorithm {
            AWS_KMS("aws:kms"),
            AES256("AES256");

            private final String value;

            EncryptionAlgorithm(String value) {
                this.value = value;
            }
        }
        public enum SuccessActionStatus {
            OK(200),
            CREATED(201),
            NO_CONTENT(204);

            private final String code;

            SuccessActionStatus(int code) {
                this.code = String.valueOf(code);
            }

            public String getCode() {
                return this.code;
            }
        }

        public enum StorageClass {
            STANDARD,
            REDUCED_REDUNDANCY,
            GLACIER,
            GLACIER_IR,
            STANDARD_IA,
            ONEZONE_IA,
            INTELLIGENT_TIERING,
            DEEP_ARCHIVE
        }

        public enum CannedAcl {
            PRIVATE("private"),
            PUBLIC_READ("public-read"),
            PUBLIC_READ_WRITE("public-read-write"),
            AWS_EXEC_READ("aws-exec-read"),
            AUTHENTICATED_READ("authenticated-read"),
            BUCKET_OWNER_READ("bucket-owner-read"),
            BUCKET_OWNER_FULL_CONTROL("bucket-owner-full-control");

            private final String cannedAcl;

            CannedAcl(String cannedAcl) {
                this.cannedAcl = cannedAcl;
            }
        }

        /**
         * Allows specifying the exact value to be used for the cache control condition
         *
         * @param value Cache control value. Example: public, max-age=7200
         * @return The {@link Builder} object
         */
        public Builder withCacheControl(String value) {
            return withCondition(CACHE_CONTROL, value);
        }

        /**
         * Allows specifying with which value the cache control condition should start with.
         *
         * @param value Cache control starting value. Example: public
         * @return The {@link Builder} object
         */
        public Builder withCacheControlStartingWith(String value) {
            return withStartingWithCondition(CACHE_CONTROL, value);
        }

        /**
         * Allows specifying which is the exact content type of the file being uploaded.
         * Example: 'audio/aac', 'text/plain'. This can be seen in the metadata information in the s3 bucket.
         * Not to be confused with file extension. To limit that use {@link KeyConditionHelper}
         *
         * @param value Content Type to be used
         * @return @return The {@link Builder} object
         */
        public Builder withContentType(String value) {
            return withCondition(CONTENT_TYPE, value);
        }

        /**
         * Allows specifying how should be the beginning of the content type for this upload.
         * Example: 'audio/aac', 'text/plain'. This can be seen in the metadata information in the s3 bucket.
         * Not to be confused with file extension. To limit that use {@link KeyConditionHelper}
         *
         * @param value Content Type to be used
         * @return @return The {@link Builder} object
         */
        public Builder withContentTypeStartingWith(String value) {
            return withStartingWithCondition(CONTENT_TYPE, value);
        }

        /**
         * Allows specifying which is the exact content disposition of the file being uploaded.
         * Example: 'inline', 'attachment'
         *
         * @param value Content Disposition value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withContentDisposition(String value) {
            return withCondition(CONTENT_DISPOSITION, value);
        }

        /**
         * Allows specifying how should be the beginning of the content disposition for this upload.
         * Example: 'inline', 'attachment'
         *
         * @param value Content Disposition to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withContentDispositionStartingWith(String value) {
            return withStartingWithCondition(CONTENT_DISPOSITION, value);
        }

        /**
         * Allows specifying which is the exact content encoding of the file being uploaded.
         * Examples: 'gzip', 'compress', etc
         *
         * @param value Content Encoding value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withContentEncoding(String value) {
            return withCondition(CONTENT_ENCODING, value);
        }

        /**
         * Allows specifying how should be the beginning of the content encoding for this upload.
         * Example: 'gzi', 'compr'
         *
         * @param value Content Encoding to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withContentEncodingStartingWith(String value) {
            return withStartingWithCondition(CONTENT_ENCODING, value);
        }

        /**
         * Allows specifying which is the exact Expires condition of the file being uploaded.
         * Example: Wed, 21 Oct 2015 07:28:00 GMT
         *
         * @param value Expires value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withExpires(String value) {
            return withCondition(EXPIRES, value);
        }

        /**
         * Allows specifying how should be the beginning of the Expires condition for this upload.
         * Example: Wed,
         *
         * @param value Expires condition value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withExpiresStartingWith(String value) {
            return withStartingWithCondition(EXPIRES, value);
        }

        /**
         * Allows specifying which is the exact success_action_redirect condition of the file being uploaded.
         * <p>
         * This condition is used to redirect the user to another page after the upload. AWS will add query parameters
         * into the end of the url such as
         * https://www.mydomain.com/?bucket=mybucket&key=test.txt&etag=%2254b0c58c7ce9f2a8b551351102ee0938%22
         * <p>
         * Example: https://www.mydomain.com // TODO suppress
         *
         * @param value success_action_redirect value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withSuccessActionRedirect(String value) {
            return withCondition(SUCCESS_ACTION_REDIRECT, value);
        }

        /**
         * Allows specifying how should be the beginning of the success_action_redirect for this upload.
         * This condition is used to redirect the user to another page after the upload
         * <p>
         * This condition is used to redirect the user to another page after the upload. AWS will add correspondent
         * query parameters into the end of the url such as
         * https://www.mydomain.com/?bucket=mybucket&key=test.txt&etag=%2254b0c58c7ce9f2a8b551351102ee0938%22
         * <p>
         * Example: https://www.mydomain. // TODO suppress
         *
         * @param value success_action_redirect condition value to be added to the policy
         * @return The {@link Builder} object
         */
        public Builder withSuccessActionRedirectStartingWith(String value) {
            return withStartingWithCondition(SUCCESS_ACTION_REDIRECT, value);
        }

        /**
         * Allows specifying which is the exact redirect condition of the file being uploaded.
         * <p>
         * This condition is used to redirect the user to another page after the upload. AWS will add query parameters
         * into the end of the url such as
         * https://www.mydomain.com/?bucket=mybucket&key=test.txt&etag=%2254b0c58c7ce9f2a8b551351102ee0938%22
         * <p>
         * Example: https://www.mydomain.com // TODO suppress
         *
         * @param value redirect value to be added to the policy
         * @return The {@link Builder} object
         */
        @Deprecated // TODO think about removing it
        public Builder withRedirect(String value) {
            return withCondition(REDIRECT, value);
        }

        /**
         * Allows specifying how should be the beginning of the redirect for this upload.
         * This condition is used to redirect the user to another page after the upload
         * <p>
         * This condition is used to redirect the user to another page after the upload. AWS will add correspondent
         * query parameters into the end of the url such as
         * https://www.mydomain.com/?bucket=mybucket&key=test.txt&etag=%2254b0c58c7ce9f2a8b551351102ee0938%22
         * <p>
         * Example: https://www.mydomain. // TODO suppress
         *
         * @param value redirect condition value to be added to the policy
         * @return The {@link Builder} object
         */
        @Deprecated // TODO think about removing it
        public Builder withRedirectStartingWith(String value) {
            return withStartingWithCondition(REDIRECT, value);
        }

        /**
         * Warning: <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/about-object-ownership.html">Aws
         * recommends that you disable ACLs except in unusual circumstances where you need to control access for each object individually.</a>.
         * <p>
         * Allows specifying how should be the acl this object.
         * <p>
         * Check <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/acl-overview.html">this</a> link to
         * understand what each one of the canned acl permissions mean.
         */
        public Builder withAcl(CannedAcl cannedAcl) {
            return withCondition(ACL, cannedAcl.cannedAcl);
        }

        /**
         * Warning: <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/about-object-ownership.html">Aws
         * recommends that you disable ACLs except in unusual circumstances where you need to control access for each object individually.</a>.
         * <p>
         * Allows specifying how should be the begging of the value of the acl for this object.
         * <p>
         * Check <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/acl-overview.html">this</a> link to
         * understand what each one of the canned acl permissions mean.
         */
        public Builder withAclStartingWith(String value) {
            return withStartingWithCondition(ACL, value);
        }

        /**
         * Allows specifying which tags should be added for the file being uploaded. If this method is used then the
         * {@link Builder#withTag(String, String)} cannot.
         * The value should be in the follow format:
         *
         * <pre> {@code
         * <Tagging>
         *     <TagSet>
         *         <Tag>
         *             <Key>MyTagName</Key>
         *             <Value>MyTagValue</Value>
         *         </Tag>
         *     </TagSet>
         * </Tagging>
         * }
         * </pre>
         * @param value The xml containing the tags to be added
         * @return The {@link Builder} object
         */
        public Builder withTagging(String value) {
            Objects.requireNonNull(value);
            //TODO adde isXml check
            return assertUniquenessAndAddTagging(value);
        }

        /**
         * Allows specifying a tag to be added for the file being uploaded. If this method is used then the
         * {@link Builder#withTagging(String)} cannot.
         *
         * @param key The key for this tag. Ex: 'MyClassification'
         * @param value The value for this tag. Ex: 'Confidential'
         * @return The {@link Builder} object
         */
        public Builder withTag(String key, String value) {
            Objects.requireNonNull(key, "Cannot add a S3 tag with a null key");
            Objects.requireNonNull(value, "Cannot add a S3 tag with a null value");
            return assertUniquenessAndAddTag(key, value);
        }

        /**
         * Allows the user to specify freely their own meta-data for the file being uploaded. It can be used multiple
         * times. Aws s3 does not validate any user meta-data. It stores user-defined metadata keys in lowercase.
         * Find more information
         * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html">here</a>
         *
         * @param metaName The name of this meta-data
         * @param value The value for this meta-data
         * @return The {@link Builder} object
         */
        public Builder withMeta(String metaName, String value) {
            Objects.requireNonNull(metaName);
            Objects.requireNonNull(value);
            conditions.put(META, new MetaCondition(EQ, metaName, value));
            return this;
        }

        /**
         * Allows the user to specify freely their own meta-data and its starting value for the file being uploaded.
         * It can be used multiple times. Aws s3 does not validate any user meta-data. It stores user-defined metadata
         * keys in lowercase. Find more information
         * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html">here</a>
         *
         * @param metaName The name of this meta-data
         * @param startingValue The starting value for this meta-data
         * @return The {@link Builder} object
         */
        public Builder withMetaStartingWith(String metaName, String startingValue) {
            Objects.requireNonNull(metaName);
            Objects.requireNonNull(startingValue);
            conditions.put(META, new MetaCondition(STARTS_WITH, metaName, startingValue));
            return this;
        }

        /**
         * Allows specifying which is the storage class to be used for the file being uploaded.
         * Find more information about each one of the options
         * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/storage-class-intro.html">here</a>
         *
         * @param storageClass Store class to be used
         * @return The {@link Builder} object
         */
        public Builder withStorageClass(StorageClass storageClass) {
            return withCondition(STORAGE_CLASS, storageClass.name());
        }

        /**
         * If the bucket is configured as a website, this condition allows specifying to redirect requests for this
         * object to another object in the same bucket or to an external URL. Note that the value must be prefixed
         * by /, http://, or https://. The length of the value is limited to 2 KB.
         * For more information
         * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html">vide the aws documentation</a>
         *
         * @param value An object in the same bucket or website to redirect to. Ex: '/anotherPage.html', 'https://www.example.com/'
         * @return The {@link Builder} object
         */
        public Builder withWebsiteRedirectLocation(String value) {
            return withCondition(WEBSITE_REDIRECT_LOCATION, value);
        }

        /**
         * Allows specifying the base64-encoded 32-bit CRC32 checksum for the file being uploaded.
         *
         * @return The {@link Builder} object
         */
        public Builder withChecksumCrc32(String checksumCrc32Value) {
            return assertUniquenessAndAdd(new ChecksumCondition(CHECKSUM_CRC32, checksumCrc32Value));
        }

        /**
         * Allows specifying the base64-encoded 32-bit CRC32C checksum for the file being uploaded.
         *
         * @return The {@link Builder} object
         */
        public Builder withChecksumCrc32c(String checksumCrc32cValue) {
            return assertUniquenessAndAdd(new ChecksumCondition(CHECKSUM_CRC32C, checksumCrc32cValue));
        }

        /**
         * Allows specifying the base64-encoded 160-bit SHA-1 digest for the file being uploaded.
         *
         * @return The {@link Builder} object
         */
        public Builder withChecksumSha1(String checksumSha1Base64Encoded) {
            return assertUniquenessAndAdd(new ChecksumCondition(CHECKSUM_SHA1, checksumSha1Base64Encoded));
        }

        /**
         * Allows specifying the base64-encoded 256-bit SHA-256 digest for the file being uploaded.
         *
         * @return The {@link Builder} object
         */
        public Builder withChecksumSha256(String checksumSha256Base64Encoded) {
            return assertUniquenessAndAdd(new ChecksumCondition(CHECKSUM_SHA256, checksumSha256Base64Encoded));
        }

        /**
         * Allows specifying which algorithm should be used for server-side encryption.
         */
        public Builder withServerSideEncryption(EncryptionAlgorithm encryptionAlgorithm) {
            return withCondition(SERVER_SIDE_ENCRYPTION, encryptionAlgorithm.value);
        }

        /**
         * Allows specifying the id of the AWS KMS KEY to be used to for server-side encryption
         *
         * @param awsKmsKeyId The value shold be in the format 'arn:aws:kms:region:acct-id:key/key-id'
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionAwsKmsKeyId(String awsKmsKeyId) {
            return withCondition(SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID, awsKmsKeyId);
        }

        /**
         * Allows specifying the encryption context for this file upload. That is, and optional set of key-value pairs
         * that can contain contextual information about the upload. It is used as ADD, that is, non-secret data that is
         * provided to encryption and decryption operations to add an additional integrity and authenticity check on the
         * encrypted data. For more information vide 'Encryption context' in the
         * <a href="https://docs.aws.amazon.com/kms/latest/developerguide/concepts.html">aws documentation.</a>
         *
         * @param awsKmsKeyId Base64 encoded json of key-value pairs
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionContext(String awsKmsKeyId) {
            return withCondition(SERVER_SIDE_ENCRYPTION_CONTEXT, awsKmsKeyId);
        }

        /**
         * Allows specifying if Amazon S3 should use an S3 Bucket Key with SSE-KMS or not.
         * When KMS encryption is used to encrypt new objects in this bucket, the bucket key reduces encryption costs by
         * lowering calls to AWS KMS. More information
         * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/bucket-key.html">here</a>.
         *
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionBucketKeyEnabled() {
            return withCondition(SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED, "true");
        }


        // TODO check if this conditions conflict with the 4 earlier ones
        /**
         * Specifies the algorithm to use to when encrypting the object.
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionCustomerAlgorithmAES256() {
            return withCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, "AES256");
        }

        /**
         * Allows specifying the base64 encoded encryption key to be used for this file upload. The key must be
         * appropriate for use with the algorithm specified. For example, using a 265 bit encryption key for AES256
         *
         * @param encryptionKeyDigestAsBase64 base64 encoded encryption key.
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionCustomerKey(String encryptionKeyDigestAsBase64) {
            return withCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, encryptionKeyDigestAsBase64);
        }

        /**
         * Allows specifying the base64 encoded 128-bit MD5 digest of the encryption key. Amazon S3 uses this header for
         * a message integrity check to ensure that the encryption key was transmitted without error.
         *
         * @param encryptionKeyDigestAsBase64 base64 encoded 128-bit MD5 digest of the encryption key
         * @return The {@link Builder} object
         */
        public Builder withServerSideEncryptionCustomerKeyMD5(String encryptionKeyDigestAsBase64) {
            return withCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5, encryptionKeyDigestAsBase64);
        }

//        AWSAccessKeyId ?

        // TODO
        // Matching Any Content
        // To configure the POST policy to allow any content within a form field, use starts-with with an empty value (""). This example allows any value for success_action_redirect:
        // ["starts-with", "$success_action_redirect", ""]

        // TODO
        // Content-Types values for a starts-with condition that include commas are interpreted as lists. Each value in the list must meet the condition for the whole condition to pass.
    }
}

