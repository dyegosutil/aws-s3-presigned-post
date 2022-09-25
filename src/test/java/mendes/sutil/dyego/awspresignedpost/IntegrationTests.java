package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import okhttp3.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
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

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.withKey;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class IntegrationTests {
    // TODO to check If you created a presigned URL using a temporary token, then the URL expires when the token expires. This is true even if the URL was created with a later expiration time.
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void generatePreSignedPostAndPerformUploadToS3(
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
            e.printStackTrace();
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

    private static Stream<Arguments> getTestCases() {
        final Region region = Region.of(System.getenv("AWS_REGION"));
        final Region incorrectRegion = Region.of(System.getenv("AWS_WRONG_REGION"));
        final String bucket = System.getenv("AWS_BUCKET");

        ZonedDateTime expirationDate = Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .plus(1, ChronoUnit.MINUTES)
                .atZone(ZoneOffset.UTC);

        return Stream.of(
                // key
                of("Should succeed while uploading file to S3 using the exact key specified in the policy",
                        region,
                        expirationDate,
                        bucket,
                        withKey("test.txt"),
                        buildConditionsMap("key","test.txt"),
                        true
                ),

                // key
                of("Should fail while uploading file to S3 using a different key then specified in the policy",
                        region,
                        expirationDate,
                        bucket,
                        withKey("test.txt"),
                        buildConditionsMap("key","different_key.txt"),
                        false
                ),

                // bucket
                of("Should fail while uploading file to S3 using a different bucket then the one configured in the policy",
                        region,
                        expirationDate,
                        "wrongbucket",
                        withKey("test.txt"),
                        buildConditionsMap("key","test.txt"),
                        false
                ),

                // region
                of("Should fail while uploading file to S3 using a different region then the one configured in the policy",
                        incorrectRegion,
                        expirationDate,
                        bucket,
                        withKey("test.txt"),
                        buildConditionsMap("key","test.txt"),
                        false
                ),

                 // expiration date
                of("Should fail while uploading file to S3 when the expiration date has passed",
                        region,
                        getInvalidExpirationDate(),
                        bucket,
                        withKey("test.txt"),
                        buildConditionsMap("key","test.txt"),
                        false
                )
        );
    }

    private static ZonedDateTime getInvalidExpirationDate() {
        return Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .minus(1, ChronoUnit.MILLIS)
                .atZone(ZoneOffset.UTC);
    }

    private static Map<String, String> buildConditionsMap(String key, String value) {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put(key,value);
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