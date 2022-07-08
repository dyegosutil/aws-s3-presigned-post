package mendes.sutil.dyego.awspresignedpost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedPost {
    private String url; //"https://$bucket.s3.eu-central-1.amazonaws.com",
    private String key;
    private String algorithm; // "AWS4-HMAC-SHA256";
    private String credential; // credentialsField;
    private String signature;
    private String date; // AMZ_DATE_FORMATTER.format(date),
    private String policy; // policyB64
}
