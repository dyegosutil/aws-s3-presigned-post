package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import lombok.Setter;

import java.util.AbstractMap;


@Getter
@Setter
// TODO add remaining fields
public class PresignedPost {
    private String url; //"https://$bucket.s3.eu-central-1.amazonaws.com", //         https://dev-de.fourthline-zip-upload.scalable.s3.eu-central-1.amazonaws.com"
    private Pair algorithm; // "AWS4-HMAC-SHA256";
    private Pair credential; // credentialsField;
    private Pair xAmzSignature; // TODO Find a patter for all of them, putting the x in front or not
    private Pair date; // AMZ_DATE_FORMATTER.format(date),
    private Pair policy; // policyB64
    private Pair key;

    PresignedPost(String url, String credential, String date, String signature, String policy, String algorithm) {
        this.url = url;
        this.credential = new Pair("x-amz-credential", credential);
        this.date = new Pair("x-amz-date", date);
        this.xAmzSignature = new Pair("x-amz-signature", signature);
        this.algorithm = new Pair("x-amz-algorithm", algorithm);
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
