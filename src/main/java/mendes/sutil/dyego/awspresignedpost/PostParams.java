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
//            String key // TODO is key mandatory?
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
            return new PostParams(bucket, region, amzExpirationDate, conditions);
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

        // TODO
        // Matching Any Content
        // To configure the POST policy to allow any content within a form field, use starts-with with an empty value (""). This example allows any value for success_action_redirect:
        // ["starts-with", "$success_action_redirect", ""]

        // TODO
        // Content-Types values for a starts-with condition that include commas are interpreted as lists. Each value in the list must meet the condition for the whole condition to pass.
    }
}
