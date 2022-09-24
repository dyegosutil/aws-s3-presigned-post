package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import lombok.Setter;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.KeyCondition;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static mendes.sutil.dyego.awspresignedpost.PostParams.ConditionMatch.EQ;

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

    private final ZonedDateTime expirationDate;
    private final List<Condition> conditions;

    private PostParams(
            String bucket,
            Region region,
            ZonedDateTime expirationDate,
            List<Condition> conditions
//            String key // TODO is key mandatory?
    ){
        this.expirationDate = expirationDate;
        this.bucket = bucket;
        this.region = region;
        this.conditions = conditions;
    }

    /**
     * Accepts all the minimum params necessary to generate a pre-signed post return a builder for PostParams // TODO fix javadoc
     *
     * @param region Region to be used in the signature
     * @param expirationDate Date until when the pre-signed post can be used.
     * @param keyCondition TODO You can use the ConditionHelper to provide the values
     * @return A PostParams builder
     */
    public static Builder builder(Region region, ZonedDateTime expirationDate, KeyCondition keyCondition){
        return new Builder(region, expirationDate, keyCondition);
    }

    enum ConditionMatch{
        EQ,
        STARTS_WITH
    }

    enum ConditionField { // TODO move it
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

    @Getter @Setter
    static class Condition {
        private ConditionField conditionField;
        private ConditionMatch conditionMatch;
        private String value;

        // TODO use lombok
        private Condition(ConditionField conditionField, ConditionMatch conditionMatch, String value) {
            this.conditionField = conditionField;
            this.conditionMatch = conditionMatch;
            this.value = value;
        }

        // TODO Remove?
        public static Condition create(ConditionField conditionField, ConditionMatch conditionMatch, String value) {
            return new Condition(
                    conditionField,
                    conditionMatch,
                    value
            );
        }
    }

    public static final class Builder {

        private final List<Condition> conditions = new ArrayList<>();

        private String bucket;
        private Region region;
//        private String key;

        private ZonedDateTime expirationDate;

        private Builder(Region region, ZonedDateTime expirationDate, KeyCondition keyCondition) {
            this.region = region;
            this.expirationDate = expirationDate;
            addKeyCondition(keyCondition);
        }

        /**
         * Adds the correspondent conditions according to the {@link KeyCondition} implementation used
         * @param keyCondition A implementation of {@link KeyCondition} to be used to add the condition
         */
        private void addKeyCondition(KeyCondition keyCondition) {
            if (keyCondition instanceof ExactKeyCondition exactKeyCondition) {
                this.conditions.add(
                        new Condition(ConditionField.KEY, EQ, exactKeyCondition.getValue())
                );
                return;
            }

            throw new IllegalArgumentException("This instance of KeyCondition with value "+keyCondition.getValue()+" in unknown");
        }

        public PostParams build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            return new PostParams(bucket, region, expirationDate, conditions);
        }

        public Builder withKey(String keyValue) {
            this.conditions.add(new Condition(ConditionField.KEY, EQ, keyValue));
            return this;
        }

//        public Builder withKeyStartingWith(String keyStartingWith) {
//            this.condition.add(new Condition(ConditionField.KEY, STARTS_WITH, keyStartingWith));
//            return this;
//        }

        public Builder withExpirationDate(ZonedDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public Builder withBucket(String bucket) {
            this.conditions.add(new Condition(ConditionField.BUCKET, EQ, bucket));
            this.bucket = bucket; // TODO double check but I dont think this has to be added in the policy since it is already in the url
            return this;
        }
    }
}
