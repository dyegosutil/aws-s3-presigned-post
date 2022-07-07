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

import software.amazon.awssdk.regions.Region;

public final class AwsSigner{

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    byte[] generateSigningKey(String secretKey, Region region, String service, ZonedDateTime date) {
        byte[] dateKey = signMac(
                "AWS4$secretKey".getBytes(StandardCharsets.UTF_8),
                DATE_FORMATTER.format(date).getBytes(
                        StandardCharsets.UTF_8
                )
        );
        byte[] dateRegionKey = signMac(dateKey, region.id().getBytes(StandardCharsets.UTF_8));
        byte[] dateRegionServiceKey = signMac(dateRegionKey, service.getBytes(StandardCharsets.UTF_8));
        return signMac(dateRegionServiceKey, "aws4_request".getBytes(StandardCharsets.UTF_8));
    }

    byte[] signMac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}