package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;

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
    private final List<Condition> conditions; // $key

    private PostParams(
            List<Condition> conditions
//            String key // TODO is key mandatory?
    ){
        this.conditions = conditions;
    }

    public static Builder builder(){
        return new Builder();
    }

    enum ConditionMatch{
        EQ,
        STARTS_WITH
    }

    enum ConditionField {
        KEY("$key"), // TODO confirm if the fields should have $ or not depending on the condition.
        ALGORITHM("$x-amz-algorithm");

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

        private List<Condition> condition = new ArrayList<>();

        private Region region;
        private String key;

        private Builder(){}

        public PostParams build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            // TODO Make sure it is build only if it will work and nothing is missing - if possible
            return new PostParams(condition);
        }

        public Builder withKey(String keyValue) {
            this.condition.add(new Condition(ConditionField.KEY, EQ, keyValue));
            return this;
        }

        public Builder withKeyStartingWith(String keyStartingWith) {
            this.condition.add(new Condition(ConditionField.KEY, STARTS_WITH, keyStartingWith));
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }
    }
}
