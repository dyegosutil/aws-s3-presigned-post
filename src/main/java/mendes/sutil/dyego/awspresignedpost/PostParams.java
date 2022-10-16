package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import mendes.sutil.dyego.awspresignedpost.domain.AmzExpirationDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ContentLengthRangeCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private final Set<Condition> conditions;

    private PostParams(
            String bucket,
            Region region,
            AmzExpirationDate amzExpirationDate,
            Set<Condition> conditions
    ){
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

        private final Set<Condition> conditions = new HashSet<>();

        private final String bucket;
        private final Region region;

        private final AmzExpirationDate amzExpirationDate;

        private Builder(Region region, AmzExpirationDate amzExpirationDate, KeyCondition keyCondition, String bucket) {
            // TODO add validation for expiration date?
            this.region = region;
            this.amzExpirationDate = amzExpirationDate;
            conditions.add(keyCondition);
            this.conditions.add(new MatchCondition(BUCKET, EQ, bucket));
            this.bucket = bucket;
        }

        public PostParams build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            return new PostParams(bucket, region, amzExpirationDate, Collections.unmodifiableSet(conditions));
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
            return assureUniquenessAndAdd(new MatchCondition(conditionField, EQ, value));
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
            return assureUniquenessAndAdd(new MatchCondition(conditionField, STARTS_WITH, value));
        }

        private Builder assureUniquenessAndAdd(MatchCondition matchCondition) {
            if (conditions.contains(matchCondition))
                throw new IllegalArgumentException(getInvalidConditionExceptionMessage(matchCondition.getConditionField()));
            this.conditions.add(matchCondition);
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
            this.conditions.add(condition);
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

            public String getCannedAcl() {
                return this.cannedAcl;
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

        // TODO
        // Matching Any Content
        // To configure the POST policy to allow any content within a form field, use starts-with with an empty value (""). This example allows any value for success_action_redirect:
        // ["starts-with", "$success_action_redirect", ""]

        // TODO
        // Content-Types values for a starts-with condition that include commas are interpreted as lists. Each value in the list must meet the condition for the whole condition to pass.
    }
}
