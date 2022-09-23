package mendes.sutil.dyego.awspresignedpost;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;

public final class AwsSigner{

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    static byte[] generateSigningKey(String secretKey, Region region, ZonedDateTime date) {
        final String service = "s3";
        byte[] dateKey = signMac(
                ("AWS4"+secretKey).getBytes(StandardCharsets.UTF_8),
                DATE_FORMATTER.format(date).getBytes(
                        StandardCharsets.UTF_8
                )
        );
        byte[] dateRegionKey = signMac(dateKey, region.id().getBytes(StandardCharsets.UTF_8));
        byte[] dateRegionServiceKey = signMac(dateRegionKey, service.getBytes(StandardCharsets.UTF_8));
        return signMac(dateRegionServiceKey, "aws4_request".getBytes(StandardCharsets.UTF_8));
    }

    public static String buildCredentialField(AwsCredentials credentials, Region region, ZonedDateTime now) {
        String accessKeyId = credentials.accessKeyId();
        String regionId = region.id();
        String date = DATE_FORMATTER.format(now);
        return accessKeyId+"/"+date+"/"+regionId+"/s3/aws4_request";
//        return credentials.accessKeyId() + "/" +
//                DATE_FORMATTER.format(ZonedDateTime.now()) + "/" +
//                region.id() + "/" +
//                "s3/aws4_request";
    }

    static byte[] signMac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hexDump(byte[] data) {
        final StringBuilder sb = new StringBuilder();
        for (byte _byte : data) {
            sb.append(HEX_CHARS[(_byte >> 4) & 0xf]);
            sb.append(HEX_CHARS[_byte & 0xf]);
        }
        return sb.toString();
    }
}