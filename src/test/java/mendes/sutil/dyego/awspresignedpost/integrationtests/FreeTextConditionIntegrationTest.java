package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.FreeTextPostParams;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import mendes.sutil.dyego.awspresignedpost.domain.AmzExpirationDate;
import mendes.sutil.dyego.awspresignedpost.domain.response.PresignedPost2;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class FreeTextConditionIntegrationTest extends IntegrationTests{

    private static final ZonedDateTime DATE = ZonedDateTime.now(Clock.systemUTC());
    private static final DateTimeFormatter YYYYMMDD_DATE_FORMATTER = getYyyyMmDdDateFormatter();
    private static final DateTimeFormatter AMZ_DATE_FORMATTER = getAmzDateFormatter();
    private static final String CREDENTIAL = getCredential();
    private static final String DATE_FOR_POLICY = AMZ_DATE_FORMATTER.format(DATE);

    /**
     * TODO should I remove the algorithm, aws_access_key, etc?
     */
    // pass security token
    @Test
    void freeTextConditionTest() {
        ZonedDateTime date = ZonedDateTime.now(Clock.systemUTC());
        String dateForCredential = DateTimeFormatter
                .ofPattern("yyyyMMdd", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC).format(date);
        String dateForPolicy = DateTimeFormatter
                .ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC).format(date);
        String credential = String.format("%s/%s/%s/s3/aws4_request", System.getenv("AWS_KEY"), dateForCredential, System.getenv("AWS_REGION"));
        Set<String[]> conditions = new HashSet<>();
        conditions.add(
                new String[]{"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"}
        );
        conditions.add(
                new String[]{
                        "eq",
                        "$x-amz-credential",
                        credential
                }
        );
        conditions.add(new String[]{"eq", "$x-amz-date", dateForPolicy});
        conditions.add(
                new String[]{"eq", "$bucket", BUCKET}
        );
        conditions.add(
                new String[]{"eq","$key","test.txt"}
        );
        FreeTextPostParams freeTextPostParams = new FreeTextPostParams(
                REGION, // TODO Is this inverted?, check if it is the same order as other PostParam
                new AmzExpirationDate(EXPIRATION_DATE), // TODO create of() ?
                date,
                conditions
        );

        // Act
        PresignedPost2 preSignedPost = new S3PostSigner(getAmazonCredentialsProvider()).create(freeTextPostParams); // TODO this could be a static method so that you don't have to call new
        System.out.println(preSignedPost);
        Map<String, String> formDataParts = createFormDataParts("key", "test.txt");
        formDataParts.put("x-amz-credential", credential);
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", dateForPolicy);
        formDataParts.put("x-amz-signature", preSignedPost.getXAmzSignature().getValue());
        formDataParts.put("policy", preSignedPost.getPolicy().getValue());
        // Note: A additional formDataParts item is added later on which should be the last one in the request: file

        Boolean wasUploadSuccessful = uploadToAws(
                formDataParts,
                String.format("https://%s.s3.%s.amazonaws.com", System.getenv("AWS_BUCKET"), System.getenv("AWS_REGION"))
        );
        assertThat(wasUploadSuccessful).isEqualTo(true);
    }

    @ParameterizedTest
    @MethodSource("freeTextConditionSessionTokenTestCases")
    void freeTextConditionSessionTokenTest(
            FreeTextPostParams freeTextPostParams,
            Map<String, String> formDataParts
    ) {
        PresignedPost2 preSignedPost = new S3PostSigner(getAmazonCredentialsProviderWithAwsSessionCredentials()).create(freeTextPostParams); // TODO this could be a static method so that you don't have to call new
        System.out.println(preSignedPost); // TODO

        formDataParts.put("x-amz-signature", preSignedPost.getXAmzSignature().getValue());
        formDataParts.put("policy", preSignedPost.getPolicy().getValue());

        Boolean wasUploadSuccessful = uploadToAws(
                formDataParts,
                getUrl()
        );
        assertThat(wasUploadSuccessful).isEqualTo(true);
    }

    private static Set<String[]> getMandatoryConditions() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(
                new String[]{
                        "eq",
                        "$x-amz-credential",
                        CREDENTIAL
                }
        );
        return conditions;
    }

    private static Set<String[]> getConditionsForAwsSts() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(
                new String[]{
                        "eq",
                        "$x-amz-credential",
                        getSessionCredential()
                }
        );
        conditions.add(
                new String[]{
                        "eq",
                        "$x-amz-security-token",
                        System.getenv("AWS_SESSION_TOKEN")
                }
        );
        return conditions;
    }

    private static Set<String[]> getCommonConditions() {
        Set<String[]> conditions = new HashSet<>();
        conditions.add(
                new String[]{"eq","$key","test.txt"}
        );
        conditions.add(
                new String[]{"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"}
        );
        conditions.add(new String[]{"eq", "$x-amz-date", DATE_FOR_POLICY});
        conditions.add(
                new String[]{"eq", "$bucket", BUCKET}
        );
        return conditions;
    }

    private static Set<String[]> getMandatoryConditions( String[] condition) {
        Set<String[]> conditions = getMandatoryConditions();
        conditions.add(condition);
        return conditions;
    }

    private static Stream<Arguments> freeTextConditionSessionTokenTestCases() {
        return Stream.of(
                // Simple test using mandatory params
                of(
                        new FreeTextPostParams(
                                REGION, // TODO Is this inverted?, check if it is the same order as other PostParam
                                new AmzExpirationDate(EXPIRATION_DATE), // TODO create of() ?
                                DATE,
                                getMandatoryConditions()
                        ),
                        getMandatoryFormDataParts()
                )
                ,
                of(
                        new FreeTextPostParams(
                                REGION,
                                new AmzExpirationDate(EXPIRATION_DATE),
                                DATE,
                                getConditionsForAwsSts()
                        ),
                        getMandatoryFormDataPartsAwsSts()
                )
        );
    }

    private static Map<String, String> getMandatoryFormDataParts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", CREDENTIAL);
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", DATE_FOR_POLICY);
        return formDataParts;
    }

    /**
     * Creates the FormDataParts with mandatory conditions additionally adding the key and value passed as arguments.
     */
    private static Object getMandatoryFormDataParts(String key, String value) {
        Map<String, String> formDataParts = getMandatoryFormDataParts();
        formDataParts.put(key, value);
        return formDataParts;
    }

    private static Object getMandatoryFormDataPartsAwsSts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getSessionCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", DATE_FOR_POLICY);
        formDataParts.put("x-amz-security-token", System.getenv("AWS_SESSION_TOKEN"));
        return formDataParts;
    }

    protected boolean uploadToAws(Map<String, String> formDataParts, String url) {
        Request request = createRequest(formDataParts, url);
        return performCallAndVerifySuccessActionRedirect(request);
    }

    private Request createRequest(Map<String, String> formDataParts, String url) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // Parameters specific for the test
        formDataParts.forEach(builder::addFormDataPart);

        // file has to be the last parameter according to aws
        builder.addFormDataPart("file", "test.txt", RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));

        MultipartBody multipartBody = builder.build();

        return new Request.Builder()
                .url(url)
                .post(multipartBody).build();
    }

    private static DateTimeFormatter getYyyyMmDdDateFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC);
    }

    private static DateTimeFormatter getAmzDateFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC);
    }

    private static String getCredential() {
        return String.format(
                "%s/%s/%s/s3/aws4_request",
                System.getenv("AWS_KEY"),
                YYYYMMDD_DATE_FORMATTER.format(DATE),
                System.getenv("AWS_REGION"));
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
}
