package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.NewPresignedPost;
import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class MandatoryPostParamsIntegrationTests extends IntegrationTests {

    /**
     * Should succeed while uploading file to S3 using the exact key specified in the policy
     */
    @Test
    void arrangeThatConditionsReturnedFromPresignedPostAreUsed_actUploadingTheFile_assertTheReturnIsSuccess() {
        // Arrange
        PostParams postParams = PostParams
                .builder(
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt")
                )
                .build();
        NewPresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).createNew(postParams);
        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequest(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(true);
    }

    /**
     * @param customizedUploadConditions Used for specifying different values then the ones provided by the presigned
     *                                   post, that is either values for "startWith" conditions or wrong values for
     *                                   asserting failure
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomizedUploadConditionsTestCases")
    void arrangeCustomizedPresignedPostConditions_actUploadTheFile_assertExpectedResult(
            String testDescription,
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyCondition keyCondition,
            Map<String, String> customizedUploadConditions,
            boolean expectedResult
    ) {
        // Arrange
        PostParams postParams = PostParams
                .builder(
                        region,
                        expirationDate,
                        bucket,
                        keyCondition
                )
                .build();
        NewPresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).createNew(postParams);
        Map<String, String> conditions = presignedPost.getConditions();
        conditions.putAll(customizedUploadConditions);

        Request request = createRequest(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private Request createRequest(Map<String, String> conditions, String url) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        conditions.forEach(builder::addFormDataPart);
        // file has to be the last parameter according to aws s3 documentation
        builder.addFormDataPart("file", "test.txt", RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));
        return new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
    }
    private static Stream<Arguments> getCustomizedUploadConditionsTestCases() {
        return Stream.of(
                // key
                of(
                        "Should fail while uploading file to S3 using a different key then specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "different_key.txt"),
                        false
                ),
                // bucket
                of(
                        "Should fail while uploading file to S3 using a different bucket then the one configured in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        "wrongbucket",
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // region
                of(
                        "Should fail while uploading file to S3 using a different region then the one configured in the policy",
                        Region.of(System.getenv("AWS_WRONG_REGION")),
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // expiration date
                of(
                        "Should fail while uploading file to S3 when the expiration date has passed",
                        REGION,
                        getInvalidExpirationDate(),
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // key starts-with
                of(
                        "Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "user/leo/file.txt"),
                        true
                ),
                // key starts-with
                of(
                        "Should fail while uploading file to S3 when key does not starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "file.txt"),
                        false
                ),
                // key starts-with anything - used also when the file name provided by the user should be used
                of(

                        "Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey(),
                        createFormDataParts("key", "myDifferentFileName.txt"),
                        true
                )
        );
    }

    private static ZonedDateTime getInvalidExpirationDate() {
        return Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .minus(1, ChronoUnit.MILLIS)
                .atZone(ZoneOffset.UTC);
    }
}
