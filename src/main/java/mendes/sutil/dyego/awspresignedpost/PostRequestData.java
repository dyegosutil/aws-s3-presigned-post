package mendes.sutil.dyego.awspresignedpost;

import software.amazon.awssdk.regions.Region;

/**
 * A pre-signed POST request that can be executed at a later time without requiring additional signing or
 * authentication.
 */
// TODO should it really be final?
public final class PostRequestData {

    // TODO which fields are alwyas going to be there?
    // TODO Double check if key is really mandatory, if it goes in the signature. If the file can be uploaded without
    //  having key in the signature
    private final String key;

    private PostRequestData(String key){
        this.key = key;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder {

        private Region region;
        private String key;

        private Builder(){}

        public PostRequestData build(){
            // TODO Identify mandatory fields and prevent building it if they are missing?
            return new PostRequestData(key);
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }
    }
}
