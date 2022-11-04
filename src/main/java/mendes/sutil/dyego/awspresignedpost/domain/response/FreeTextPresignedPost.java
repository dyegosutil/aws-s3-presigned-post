package mendes.sutil.dyego.awspresignedpost.domain.response;

import java.util.AbstractMap;

/**
 * TODO change to map
 */
public class FreeTextPresignedPost {
    private final Pair xAmzSignature; // TODO Find a patter for all of them, putting the x in front or not
    private final Pair policy;

    public FreeTextPresignedPost(String signature, String policy) {
        this.xAmzSignature = new Pair("x-amz-signature", signature);
        this.policy = new Pair("policy", policy);
    }

    // TODO Rename to a better name? Param?
    public static class Pair extends AbstractMap.SimpleImmutableEntry<String, String> {
        public Pair(String key, String value) {
            super(key, value);
        }

        @Override
        public String getKey() {
            return super.getKey();
        }

        @Override
        public String getValue() {
            return super.getValue();
        }

        @Override
        public String toString() {
            return "key='"+getKey()+"' value='"+getValue()+"'}";
        }
    }

    public Pair getxAmzSignature() {
        return xAmzSignature;
    }

    public Pair getPolicy() {
        return policy;
    }
}
