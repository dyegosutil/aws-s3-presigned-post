package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import okhttp3.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.*;
import static org.junit.jupiter.params.provider.Arguments.of;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class IntegrationTests {
    private static final Region REGION = Region.of(System.getenv("AWS_REGION"));
    private static final ZonedDateTime EXPIRATION_DATE = Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
            .plus(1, ChronoUnit.MINUTES)
            .atZone(ZoneOffset.UTC);
    private static final String BUCKET = System.getenv("AWS_BUCKET");

    // TODO to check If you created a presigned URL using a temporary token, then the URL expires when the token expires. This is true even if the URL was created with a later expiration time.

    @Disabled
    /**
     * Generates the pre-signed post using the minimum mandatory params and performs the upload to S3 using a http client.
     *
     * @param testDescription
     * @param region
     * @param expirationDate
     * @param bucket
     * @param keyCondition
     * @param formDataParts
     * @param expectedResult
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesMandatoryParams")
    void testWithMandatoryParams(
            String testDescription,
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyCondition keyCondition,
            Map<String,String> formDataParts,
            Boolean expectedResult
    ) {
        //TODO check about the token part
        //                                        .withToken
        //                                        ("FwoGZXIvYXdzEAMaDJnjMcCzZ05MxE5udCKzAd8acj8V3dKJfJbtEASA07VbfGfsSsd5MXSC4PnsBr8q4VXbseNaV6IXIeAknFF0w4+Vcy/2q2krRqxXYhaQBKrTqj0f/622MlaS+DCQc6rJm0JxG9p0Ws3ftDWC89Nm85bRoFmNucBpVIr1eakuzFknTIqtm5PuLlYiis6ybiTRrUQ8kXmEjy8u5BjSORKScjMVy5WQmcfcxTIodyonRVbyGr6tJo4URs7Iu2CKJL6LQgURKJOa8pUGMi0Hcs2nE/IEApn7izSTyUmgfTgzNcGJsTbBeeJHM49RNOaCcI8IVkRlZlJFE28=")

        PostParams postParams = PostParams
                .builder(
                        region,
                        expirationDate,
                        bucket,
                        keyCondition
                )
                .build();

        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).create(postParams);
        System.out.println(presignedPost); // TODO Check about logging for tests, would be nice to know why it failed in GIT

        Boolean wasUploadSuccessful = false;
        try {
            wasUploadSuccessful = uploadToAws(presignedPost, formDataParts);
        } catch (Exception e) {
            e.printStackTrace(); // TODO is this good?
        }
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    /**
     * Generates the pre-signed post using the mandatory params and also optional params performing the upload to S3.
     *
     * @param testDescription
     * @param postParams
     * @param formDataParts
     * @param expectedResult
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesOptionalParams")
    void testWithOptionalParams(
            String testDescription,
            PostParams postParams,
            Map<String,String> formDataParts,
            Boolean expectedResult
    ) {
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).create(postParams);
        System.out.println(presignedPost); // TODO Check about logging for tests, would be nice to know why it failed in GIT

        Boolean wasUploadSuccessful = false;
        try {
            wasUploadSuccessful = uploadToAws(presignedPost, formDataParts);
        } catch (Exception e) {
            e.printStackTrace(); // TODO is this good?
        }
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    /**
     * TODO check if this is really necessary, if it could be just done using not aws lib code
     * @return The AwsCredentialsProvider to be used to create the pre-signed post
     */
    private AwsCredentialsProvider getAmazonCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(System.getenv("AWS_KEY"),
                        System.getenv("AWS_SECRET"))
        );
    }

    /**
     * Creates a {@link PostParams.Builder} with the minimum mandatory parameters
     * @return A {@link PostParams.Builder}
     */
    private static PostParams.Builder createDefaultPostParamBuilder() {
        return PostParams
                .builder(
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey()
                );
    }

    private static Stream<Arguments> getTestCasesOptionalParams() {
        return Stream.of(
                // content-length-range
                of("Should succeed while uploading file to S3 when it's size is between the minimum and maximum specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(7, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of("Should succeed while uploading file to S3 when it's size is of the exact size specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(14, 14)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of("Should fail while uploading file to S3 when it's size is over the maximum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(1, 2)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // content-length-range
                of("Should fail while uploading file to S3 when it's size is under the minimum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(15, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // Cache-Control
                of("Should succeed while uploading file to S3 when the cache-control specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of("Should fail while uploading file to S3 when the cache-control specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                ),
                // Cache-Control
                of("Should succeed while uploading file to S3 when the cache-control specified starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControlStartingWith("public,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of("Should fail while uploading file to S3 when the cache-control specified does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                )
        );
    }

    private static Stream<Arguments> getTestCasesMandatoryParams() {
        final Region incorrectRegion = Region.of(System.getenv("AWS_WRONG_REGION"));

        return Stream.of(
                // key
                of("Should succeed while uploading file to S3 using the exact key specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key","test.txt"),
                        true
                ),

                // key
                of("Should fail while uploading file to S3 using a different key then specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key","different_key.txt"),
                        false
                ),

                // bucket
                of("Should fail while uploading file to S3 using a different bucket then the one configured in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        "wrongbucket",
                        withKey("test.txt"),
                        createFormDataParts("key","test.txt"),
                        false
                ),

                // region
                of("Should fail while uploading file to S3 using a different region then the one configured in the policy",
                        incorrectRegion,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key","test.txt"),
                        false
                ),

                 // expiration date
                of("Should fail while uploading file to S3 when the expiration date has passed",
                        REGION,
                        getInvalidExpirationDate(),
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key","test.txt"),
                        false
                ),

                // key starts-with
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key","user/leo/file.txt"),
                        true
                ),

                // key starts-with
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key","file.txt"),
                        false
                ),

                // key starts-with anything - used also when the file name provided by the user should be used
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey(),
                        createFormDataParts("key","file.txt"),
                        true
                )
        );
    }

    private static ZonedDateTime getInvalidExpirationDate() {
        return Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .minus(1, ChronoUnit.MILLIS)
                .atZone(ZoneOffset.UTC);
    }

    private static Map<String, String> createFormDataParts(String key, String value) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key,value);
        return formDataParts;
    }

    private static Map<String, String> createFormDataPartsWithKeyCondition(String key, String value) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key,value);
        formDataParts.put("key","${filename}");
        return formDataParts;
    }

    /**
     * TODO Change to a better http client since okhttp does not give as much information as postman when a 400 happens.
     * If errors happens here, better debug with postman
     *
     * @param presignedPost
     * @return
     * @throws IOException In case the upload is not successful
     */
    private boolean uploadToAws(PresignedPost presignedPost, Map<String, String> formDataParts) throws IOException {
        final OkHttpClient client = new OkHttpClient();

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

        Request request = new Request.Builder().url(presignedPost.getUrl()).post(multipartBody).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response + response.message());
            } else {
                return true;
            }
        }
    }
}