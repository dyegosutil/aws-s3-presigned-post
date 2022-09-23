package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static mendes.sutil.dyego.awspresignedpost.PostParams.ConditionMatch.EQ;
import static mendes.sutil.dyego.awspresignedpost.PostParams.ConditionMatch.STARTS_WITH;

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

    public static Builder builder(ZonedDateTime expirationDate){
        return new Builder(expirationDate);
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
        public static Condition create(ConditionField conditionField, ConditionMatch conditionMatch, String value) {
            return new Condition(
                    conditionField,
                    conditionMatch,
                    value
            );
        }
    }

    public static final class Builder {

        private final List<Condition> condition = new ArrayList<>();

        private String bucket;
        private Region region;
//        private String key;

        private ZonedDateTime expirationDate;
        private Builder(ZonedDateTime expirationDate){
            this.expirationDate = expirationDate;
        }

        public PostParams build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            return new PostParams(bucket, region, expirationDate, condition);
        }

        public Builder withKey(String keyValue) {
            this.condition.add(new Condition(ConditionField.KEY, EQ, keyValue));
            return this;
        }

        public Builder withKeyStartingWith(String keyStartingWith) {
            this.condition.add(new Condition(ConditionField.KEY, STARTS_WITH, keyStartingWith));
            return this;
        }

        public Builder withExpirationDate(ZonedDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder withBucket(String bucket) {
            this.condition.add(new Condition(ConditionField.BUCKET, EQ, bucket));
            this.bucket = bucket; // TODO double check but I dont think this has to be added in the policy since it is already in the url
            return this;
        }
    }
}
