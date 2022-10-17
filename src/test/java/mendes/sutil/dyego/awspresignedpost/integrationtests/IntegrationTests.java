package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import okhttp3.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.withAnyKey;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO Check to use S3 local?
 * // Reading credentials from ENV-variables
 *         AwsCredentialsProvider awsCredentialsProvider = DefaultCredentialsProvider.builder().build();
 */
public class IntegrationTests {

    protected static final Region REGION = Region.of(System.getenv("AWS_REGION"));
    protected static final ZonedDateTime EXPIRATION_DATE = Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
            .plus(1, ChronoUnit.MINUTES)
            .atZone(ZoneOffset.UTC);
    protected static final String BUCKET = System.getenv("AWS_BUCKET");

    protected void createPreSignedPostAndUpload(PostParams postParams, Map<String, String> formDataParts, Boolean expectedResult) {
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).create(postParams);
        System.out.println(presignedPost); // TODO Check about logging for tests, would be nice to know why it failed in GIT
        Boolean wasUploadSuccessful = uploadToAws(presignedPost, formDataParts);
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    /**
     * TODO check if this is really necessary, if it could be just done using not aws lib code
     *
     * @return The AwsCredentialsProvider to be used to create the pre-signed post
     */
    protected AwsCredentialsProvider getAmazonCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(System.getenv("AWS_KEY"),
                        System.getenv("AWS_SECRET"))
        );
    }

    /**
     * TODO Change to a better http client since okhttp does not give as much information as postman when a 400 happens.
     * If errors happens here, better debug with postman
     *
     * @param presignedPost
     * @return
     */
    protected boolean uploadToAws(PresignedPost presignedPost, Map<String, String> formDataParts) {
        Request request = createRequest(presignedPost, formDataParts);
        return performCallAndVerifySuccessActionRedirect(request);
    }

    private boolean performCallAndVerifySuccessActionRedirect(Request request) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            return checkSuccessAndPrintResponseIfError(response);
        } catch (Exception e) {
            System.err.println(e); // TODO fix
            return false;
        }
    }

    private Request createRequest(PresignedPost presignedPost, Map<String, String> formDataParts) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // below are all the parameters that are required for the upload to be successful. Apart from file that has to be the last one
                .addFormDataPart(presignedPost.getCredential().getKey(), presignedPost.getCredential().getValue())
                .addFormDataPart(presignedPost.getXAmzSignature().getKey(), presignedPost.getXAmzSignature().getValue()) // TODO fix this
                .addFormDataPart(presignedPost.getAlgorithm().getKey(), presignedPost.getAlgorithm().getValue())
                .addFormDataPart(presignedPost.getDate().getKey(), presignedPost.getDate().getValue())
                .addFormDataPart(presignedPost.getPolicy().getKey(), presignedPost.getPolicy().getValue());

        // Parameters specific for the test
        formDataParts.forEach(builder::addFormDataPart);

        // file has to be the last parameter according to aws
        builder.addFormDataPart("file", "test.txt", RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));

        MultipartBody multipartBody = builder.build();

        return new Request.Builder()
                .url(presignedPost.getUrl())
                .post(multipartBody).build();
    }

    private boolean checkSuccessAndPrintResponseIfError(Response response) {
        if (!response.isSuccessful()) {
            System.err.println(new IOException("Unexpected code " + response + response.message()));  // TODO change it
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

    protected static Map<String, String> createFormDataPartsWithKeyCondition(String key, String value, String key2, String value2) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key, value);
        formDataParts.put(key2, value2);
        formDataParts.put("key", "${filename}");
        return formDataParts;
    }

    protected boolean uploadToAwsCheckingSuccessActionStatus(
            PresignedPost presignedPost, Map<String, String> formDataParts, int expectedResponseCode
    ) {
        Request request = createRequest(presignedPost, formDataParts);
        return performCallAndVerifySuccessActionStatus(request, expectedResponseCode);
    }

    private boolean performCallAndVerifySuccessActionStatus(Request request, int expectedResponseCode) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            assertThat(response.code()).isEqualTo(expectedResponseCode);
            return checkSuccessAndPrintResponseIfError(response);
        } catch (Exception e) {
            System.err.println(e); // TODO fix
            return false;
        }
    }

    protected boolean uploadToAwsCheckingRedirect(PresignedPost presignedPost, Map<String, String> formDataParts, String redirectHttpClientField) {
        Request request = createRequest(presignedPost, formDataParts);
        String successActionRedirect = formDataParts.get(redirectHttpClientField); // TODO User constants?
        return performCallAndVerifySuccessActionRedirect(request, successActionRedirect);
    }

    private boolean performCallAndVerifySuccessActionRedirect(Request request, String successActionRedirect) {
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            HttpUrl httpUrl = response.request().url();
            String responseRedirectUrl = httpUrl.scheme() + "://" + httpUrl.host();
            assertThat(responseRedirectUrl).isEqualTo(successActionRedirect);

            return checkSuccessAndPrintResponseIfError(response);
        } catch (Exception e) {
            System.err.println(e); // TODO fix
            return false;
        }
    }
}
