package io.github.dyegosutil.awspresignedpost.signer;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.dyegosutil.awspresignedpost.AmzDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AwsSigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSigner.class);
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    static byte[] generateSigningKey(String secretKey, Region region, AmzDate xAmzDate) {
        final String service = "s3";
        byte[] dateKey =
                signMac(
                        ("AWS4" + secretKey).getBytes(UTF_8),
                        xAmzDate.formatForSigningKey().getBytes(UTF_8));
        byte[] dateRegionKey = signMac(dateKey, region.id().getBytes(UTF_8));
        byte[] dateRegionServiceKey = signMac(dateRegionKey, service.getBytes(UTF_8));
        return signMac(dateRegionServiceKey, "aws4_request".getBytes(UTF_8));
    }

    static byte[] signMac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error("Error while signing Mac with algorithm HmacSHA256", e);
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
