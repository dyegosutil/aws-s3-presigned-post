package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import mendes.sutil.dyego.awspresignedpost.domain.AmzExpirationDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition.ConditionMatch.EQ;

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
    private final List<Condition> conditions;

    private PostParams(
            String bucket,
            Region region,
            AmzExpirationDate amzExpirationDate,
            List<Condition> conditions
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
     * @return A PostParams builder which allows more fine grained conditions to be added
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

        private final List<Condition> conditions = new ArrayList<>();

        private final String bucket;
        private final Region region;
//        private String key;

        private final AmzExpirationDate amzExpirationDate;

        private Builder(Region region, AmzExpirationDate amzExpirationDate, KeyCondition keyCondition, String bucket) {
            // TODO add validation for expiration date?
            this.region = region;
            this.amzExpirationDate = amzExpirationDate;
            conditions.add(keyCondition);
            this.conditions.add(new Condition(Condition.ConditionField.BUCKET, EQ, bucket));
            this.bucket = bucket;
        }

        public PostParams build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            return new PostParams(bucket, region, amzExpirationDate, conditions);
        }

//        public Builder withKeyStartingWith(String keyStartingWith) {
//            this.condition.add(new Condition(ConditionField.KEY, STARTS_WITH, keyStartingWith));
//            return this;
//        }
    }
}
