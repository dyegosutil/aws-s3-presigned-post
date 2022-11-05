package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import mendes.sutil.dyego.awspresignedpost.postparams.FreeTextPostParams;
import mendes.sutil.dyego.awspresignedpost.result.FreeTextPresignedPost;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

//@Disabled
public class FreeTextIntegrationTest extends IntegrationTests {

    private static final ZonedDateTime DATE = ZonedDateTime.now(Clock.systemUTC());
    private static final DateTimeFormatter YYYYMMDD_DATE_FORMATTER = getYyyyMmDdDateFormatter();

    @ParameterizedTest(name = "{0}")
    @MethodSource("freeTextConditionSessionTokenTestCases")
    void freeTextConditionSessionTokenTest(
            String testDescription,
            Map<String, String> formDataParts
    ) {
        assertThat(
                uploadToAws(formDataParts, getUrl())
        ).isTrue();
    }

    private static Stream<Arguments> freeTextConditionSessionTokenTestCases() {
        return Stream.of(
                getSimpleUploadTestCase(),
                getAwsStsTokenTestCase(),
                getEncryptionWithCustomerKeyTestCase()
        );
    }

    private static Arguments getEncryptionWithCustomerKeyTestCase() {
        FreeTextPostParams freeTextPostParams = getFreeTextPostParams(getConditionsForUploadWithCustomerEncryptionKey());
        FreeTextPresignedPost preSignedPost = S3PostSigner.create(freeTextPostParams, getAmazonCredentialsProvider());
        return of(
                "Should upload file using free text post params where file encryption is used with key specified by the user. One of the most complex cases",
                getFormData(preSignedPost, getFormDataPartsForUploadWithCustomerEncryptionKey())
        );
    }

    private static Arguments getAwsStsTokenTestCase() {
        FreeTextPostParams freeTextPostParams = getFreeTextPostParams(getConditionsForAwsSts());
        FreeTextPresignedPost preSignedPost = S3PostSigner.create(
                freeTextPostParams,
                getAmazonCredentialsProviderWithAwsSessionCredentials()
        );
        return of(
                "Should upload file using free text post params where aws sts token is used",
                getFormData(preSignedPost, getFormDataPartsAwsSts())
        );
    }

    private static Map<String, String> getFormData(FreeTextPresignedPost preSignedPost, Map<String, String> formDataParts) {
        formDataParts.put("x-amz-signature", preSignedPost.getxAmzSignature().getValue());
        formDataParts.put("policy", preSignedPost.getPolicy().getValue());
        return formDataParts;
    }

    /**
     * @return Simple test using mandatory params
     */
    private static Arguments getSimpleUploadTestCase() {
        FreeTextPostParams freeTextPostParams = getFreeTextPostParams(getMandatoryConditions());
        FreeTextPresignedPost preSignedPost = S3PostSigner.create(
                freeTextPostParams,
                getAmazonCredentialsProvider()
        );
        return of(
                "Should upload file using free text post params where mandatory params are used. This is the simplest upload condition possible",
                getFormData(preSignedPost, getMandatoryFormDataParts())
        );
    }

    private static Set<String[]> getMandatoryConditions() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(new String[]{"eq", "$x-amz-credential", getCredential()});
        return conditions;
    }

    private static Set<String[]> getConditionsForUploadWithCustomerEncryptionKey() { // TODO have a general look in the free text part to see if there is anything else (code, functionality) that should be extracted out of it, meaning that otherwise it would not work for certainc ases
        Set<String[]> conditions = getMandatoryConditions();
        conditions.add(new String[]{"eq", "$x-amz-server-side-encryption-customer-algorithm", "AES256"});
        conditions.add(new String[]{"eq", "$x-amz-server-side-encryption-customer-key", encodeToBase64(encryptionKey256bits)});
        conditions.add(new String[]{"eq", "$x-amz-server-side-encryption-customer-key-MD5", generateEncryptionKeyMD5DigestAsBase64(encryptionKey256bits)});
        return conditions;
    }

    private static Set<String[]> getConditionsForAwsSts() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(new String[]{"eq", "$x-amz-credential", getSessionCredential()});
        conditions.add(new String[]{"eq", "$x-amz-security-token", System.getenv("AWS_SESSION_TOKEN")});
        return conditions;
    }

    private static Set<String[]> getCommonConditions() {
        Set<String[]> conditions = new HashSet<>();
        conditions.add(new String[]{"eq", "$key", "test.txt"});
        conditions.add(new String[]{"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"});
        conditions.add(new String[]{"eq", "$x-amz-date", getAmzDateFormatter().format(DATE)});
        conditions.add(new String[]{"eq", "$bucket", BUCKET});
        return conditions;
    }

    private static Map<String, String> getMandatoryFormDataParts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", getAmzDateFormatter().format(DATE));
        return formDataParts;
    }

    private static Map<String, String> getFormDataPartsForUploadWithCustomerEncryptionKey() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", getAmzDateFormatter().format(DATE));
        formDataParts.put("x-amz-server-side-encryption-customer-algorithm", "AES256");
        formDataParts.put("x-amz-server-side-encryption-customer-key", encodeToBase64(encryptionKey256bits));
        formDataParts.put("x-amz-server-side-encryption-customer-key-MD5", generateEncryptionKeyMD5DigestAsBase64(encryptionKey256bits));

        return formDataParts;
    }

    private static Map<String, String> getFormDataPartsAwsSts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getSessionCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", getAmzDateFormatter().format(DATE));
        formDataParts.put("x-amz-security-token", System.getenv("AWS_SESSION_TOKEN"));
        return formDataParts;
    }

    protected boolean uploadToAws(Map<String, String> formDataParts, String url) {
        Request request = createRequest(formDataParts, url);
        return postFileIntoS3(request);
    }

    private Request createRequest(Map<String, String> formDataParts, String url) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        formDataParts.forEach(builder::addFormDataPart);
        // file has to be the last parameter according to aws
        builder.addFormDataPart("file", "test.txt", RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));
        MultipartBody multipartBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
    }

    private static String getCredential() {
        return String.format(
                "%s/%s/%s/s3/aws4_request",
                System.getenv("AWS_KEY"),
                YYYYMMDD_DATE_FORMATTER.format(DATE),
                System.getenv("AWS_REGION")
        );
    }

    private static String getSessionCredential() {
        return String.format(
                "%s/%s/%s/s3/aws4_request",
                System.getenv("AWS_SESSION_KEY"),
                YYYYMMDD_DATE_FORMATTER.format(DATE),
                System.getenv("AWS_REGION"));
    }

    private String getUrl() {
        return String.format(
                "https://%s.s3.%s.amazonaws.com",
                System.getenv("AWS_BUCKET"),
                System.getenv("AWS_REGION")
        );
    }

    private static FreeTextPostParams getFreeTextPostParams(Set<String[]> conditions) {
        return new FreeTextPostParams(
                REGION, // TODO Is this inverted?, check if it is the same order as other PostParam
                EXPIRATION_DATE,
                DATE,
                conditions
        );
    }
}
