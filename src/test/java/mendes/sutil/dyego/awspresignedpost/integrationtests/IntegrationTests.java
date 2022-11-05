package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static mendes.sutil.dyego.awspresignedpost.conditions.helper.KeyConditionHelper.withAnyKey;

/**
 * TODO Check to use S3 local?
 * // Reading credentials from ENV-variables
 *         AwsCredentialsProvider awsCredentialsProvider = DefaultCredentialsProvider.builder().build();
 */
public class IntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);

    protected static final Region REGION = Region.of(System.getenv("AWS_REGION"));
    protected static final ZonedDateTime EXPIRATION_DATE = Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
            .plus(1, ChronoUnit.MINUTES)
            .atZone(ZoneOffset.UTC);

    protected static final String BUCKET = System.getenv("AWS_BUCKET");
    protected static final String encryptionKey256bits = "PcI54Y7WIu8aU1fSoEN&34mS#$*S21%3";

    boolean postFileIntoS3(Request request) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            return checkSuccessAndPrintResponseIfError(response);
        } catch (Exception e) {
            LOGGER.error("Error while performing call to post file in S3 using http client", e);
            return false;
        }
    }

    String postFileIntoS3ReturningRedirect(Request request) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            HttpUrl httpUrl = response.request().url();
            checkSuccessAndPrintResponseIfError(response);
            return httpUrl.scheme() + "://" + httpUrl.host();
        } catch (Exception e) {
            LOGGER.error("Error while performing call to post file in S3 using http client", e);
            throw new RuntimeException(e);
        }
    }

    int postFileIntoS3ReturningSuccessActionStatus(Request request) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            checkSuccessAndPrintResponseIfError(response);
            return response.code();
        } catch (Exception e) {
            LOGGER.error("Error while performing call to post file in S3 using http client", e);
            throw new RuntimeException(e);
        }
    }

    private boolean checkSuccessAndPrintResponseIfError(Response response) {
        if (!response.isSuccessful()) {
            LOGGER.error("Http client call to post file into s3 failed. Unexpected code {} {}", response, response.message());
            try {
                String responseXml = new String(Objects.requireNonNull(response.body()).bytes(), StandardCharsets.UTF_8);
                LOGGER.error(" Response xml: {}", responseXml);
            } catch (IOException e) {
                LOGGER.error("Error while getting the xml response from failed upload", e);
                throw new RuntimeException(e);
            }
            return false;
        }
        return true;
    }

    protected static Map<String, String> createFormDataParts(String key, String value) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key, value);
        return formDataParts;
    }

    /**
     * Creates a {@link PostParams.Builder} with the minimum mandatory parameters
     *
     * @return A {@link PostParams.Builder}
     */
    protected static PostParams.Builder createDefaultPostParamBuilder() {
        return PostParams
                .builder(
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey()
                );
    }

    protected static Map<String, String> createFormDataPartsWithKeyCondition(String key, String value) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key, value);
        formDataParts.put("key", "${filename}");
        return formDataParts;
    }

    // TODO make this better, repeated code here
    protected static Map<String, String> createFormDataPartsWithKeyCondition(String key, String value, String key2, String value2) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key, value);
        formDataParts.put(key2, value2);
        formDataParts.put("key", "${filename}");
        return formDataParts;
    }

    protected static Map<String, String> createFormDataPartsWithKeyCondition(
            String key,
            String value,
            String key2,
            String value2,
            String key3,
            String value3
    ) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key, value);
        formDataParts.put(key2, value2);
        formDataParts.put(key3, value3);
        formDataParts.put("key", "${filename}");
        return formDataParts;
    }

    protected static String encodeToBase64(String valueToBeBase64Encoded) { // TODO insert notNull annotation?
        return Base64.getEncoder().encodeToString(valueToBeBase64Encoded.getBytes(StandardCharsets.UTF_8));
    }

    private static String encodeToBase64(byte[] valueToBeBase64Encoded) {
        return Base64.getEncoder().encodeToString(valueToBeBase64Encoded);
    }

    protected static String generateEncryptionKeyMD5DigestAsBase64(String encryptionKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(encryptionKey.getBytes());
            return encodeToBase64(md.digest());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error while generating MD5 digest", e);
            throw new RuntimeException(e);
        }
    }

    // TODO possiblly shorter the name?
    protected Request createRequestFromConditions(Map<String, String> conditions, String url) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        conditions.forEach(builder::addFormDataPart);
        // file has to be the last parameter according to aws s3 documentation
        builder.addFormDataPart("file", "test.txt", RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));
        return new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
    }
}
