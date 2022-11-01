package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;

import java.util.AbstractMap;
import java.util.Map;


@Getter
/**
 * TODO say that it will return the ${filename} for key
 * TODO check if is better to simply return a map and that is it.
 */
public class PresignedPost {
    private final String url;
    private final Pair algorithm;
    private final Pair credential;
    private final Pair xAmzSignature; // TODO Find a pattern for all of them, putting the x in front or not
    private final Pair date;
    private final Pair policy;
    private final Pair key;

    private final Map<String, String> conditions;

    PresignedPost(String url, String credential, String date, String signature, String policy, String algorithm, String key, Map<String, String> conditions) {
        // TODO check not null?
        this.url = url;
        this.credential = new Pair("x-amz-credential", credential);
        this.date = new Pair("x-amz-date", date);
        this.xAmzSignature = new Pair("x-amz-signature", signature);
        this.algorithm = new Pair("x-amz-algorithm", algorithm);
        this.policy = new Pair("policy", policy);
        this.key = new Pair("key", key);
        this.conditions = conditions;
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

    /**
     * TODO print items in the map as well? mask credential?
     */
    @Override
    public String toString() {
        return "PresignedPost{" +
                "url='" + url + '\'' +
                "\n, algorithm=" + algorithm +
                "\n, credential=" + credential +
                "\n, xAmzSignature=" + xAmzSignature +
                "\n, date=" + date +
                "\n, policy=" + policy +
                "\n, key=" + key +
                '}';
    }
}
