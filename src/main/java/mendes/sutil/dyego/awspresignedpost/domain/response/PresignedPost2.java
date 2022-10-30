package mendes.sutil.dyego.awspresignedpost.domain.response;

import lombok.Getter;
import lombok.Setter;
import mendes.sutil.dyego.awspresignedpost.PresignedPost;

import java.util.AbstractMap;


@Getter
@Setter
// TODO add remaining fields
public class PresignedPost2 { // TODO Definitely chose a better name
    private Pair xAmzSignature; // TODO Find a patter for all of them, putting the x in front or not
    private Pair policy; // policyB64

    private PresignedPost.Pair credential; // credentialsField;

    private PresignedPost.Pair date; // AMZ_DATE_FORMATTER.format(date),

    public PresignedPost2(String signature, String date, String policy) {
        this.xAmzSignature = new Pair("x-amz-signature", signature);
//        this.credential = new PresignedPost.Pair("x-amz-credential", credential);
        this.date = new PresignedPost.Pair("x-amz-date", date);
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

    /**
     * TODO watch out to not log PII info or sensitive info
     * @return
     */
    @Override
    public String toString() {
        return "PresignedPost2{" +
                "\nxAmzSignature=" + xAmzSignature +
                "\n, credential=" + credential +
                "\n, date=" + date +
                "\n, policy=" + policy +
                '}';
    }
}
