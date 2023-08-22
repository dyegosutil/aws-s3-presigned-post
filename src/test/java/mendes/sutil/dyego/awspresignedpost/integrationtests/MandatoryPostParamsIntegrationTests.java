package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.signer.S3PostSigner;
import mendes.sutil.dyego.awspresignedpost.conditions.key.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.conditions.key.KeyCondition;
import mendes.sutil.dyego.awspresignedpost.conditions.key.KeyStartingWithCondition;
import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.presigned.PreSignedPost;
import okhttp3.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.getAmazonCredentialsProvider;
import static mendes.sutil.dyego.awspresignedpost.conditions.KeyConditionUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class MandatoryPostParamsIntegrationTests extends IntegrationTests {

    @Test
    void shouldUploadFileWithMandatoryParams() {
        // Arrange
        PostParams postParams =
                PostParams.builder(REGION, EXPIRATION_DATE, BUCKET, withKey("test.txt")).build();
        PreSignedPost presignedPost =
                S3PostSigner.sign(postParams, getAmazonCredentialsProvider());
        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isTrue();
    }

    /**
     * @param customizedUploadConditions Used for specifying different values than the ones provided
     *     by the pre-signed post, which are either values for "startWith" conditions or wrong
     *     values for asserting failure
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomizedUploadConditionsTestCases")
    void shouldTryToPerformUploadMatchingWithExpectedResult(
            String testDescription,
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyCondition keyCondition,
            Map<String, String> customizedUploadConditions,
            boolean expectedResult) {
        // Arrange
        PostParams postParams = createPostParams(region, expirationDate, bucket, keyCondition);
        PreSignedPost presignedPost =
                S3PostSigner.sign(postParams, getAmazonCredentialsProvider());
        Map<String, String> conditions = presignedPost.getConditions();
        conditions.putAll(customizedUploadConditions);

        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private PostParams createPostParams(
            Region region, ZonedDateTime expirationDate, String bucket, KeyCondition keyCondition) {
        if (keyCondition instanceof ExactKeyCondition) {
            return createPostParamsWithExactKeyCondition(
                    region, expirationDate, bucket, keyCondition);
        }
        if (keyCondition instanceof KeyStartingWithCondition) {
            return createPostParamsWithKeyStartingWithCondition(
                    region, expirationDate, bucket, keyCondition);
        }
        throw new IllegalArgumentException(
                "Cannot create PostParams. Only ExactKeyCondition and KeyStartingWithCondition are"
                        + " supported");
    }

    private PostParams createPostParamsWithExactKeyCondition(
            Region region, ZonedDateTime expirationDate, String bucket, KeyCondition keyCondition) {
        return PostParams.builder(
                        region, expirationDate, bucket, castToExactKeyCondition(keyCondition))
                .build();
    }

    private PostParams createPostParamsWithKeyStartingWithCondition(
            Region region, ZonedDateTime expirationDate, String bucket, KeyCondition keyCondition) {
        return PostParams.builder(
                        region,
                        expirationDate,
                        bucket,
                        castToKeyStartingWithCondition(keyCondition))
                .build();
    }

    private ExactKeyCondition castToExactKeyCondition(KeyCondition keyCondition) {
        return (ExactKeyCondition) keyCondition;
    }

    private KeyStartingWithCondition castToKeyStartingWithCondition(KeyCondition keyCondition) {
        return (KeyStartingWithCondition) keyCondition;
    }

    private static Stream<Arguments> getCustomizedUploadConditionsTestCases() {
        return Stream.of(
                // key
                of(
                        "Should fail while uploading file to S3 using a different key then"
                                + " specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "different_key.txt"),
                        false),
                // bucket
                of(
                        "Should fail while uploading file to S3 using a different bucket then the"
                                + " one configured in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        "wrongbucket",
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false),
                // region
                of(
                        "Should fail while uploading file to S3 using a different region then the"
                                + " one configured in the policy",
                        Region.of(System.getenv("AWS_WRONG_REGION")),
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false),
                // expiration date - Already tested in PostParamsTest
                // key starts-with
                of(
                        "Should succeed while uploading file to S3 when key correctly starts-with"
                                + " the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "user/leo/file.txt"),
                        true),
                // key starts-with
                of(
                        "Should fail while uploading file to S3 when key does not starts-with the"
                                + " value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "file.txt"),
                        false),
                // key starts-with anything - used also when the file name provided by the user
                // should be
                // used
                of(
                        "Should succeed while uploading file to S3 when key correctly starts-with"
                                + " the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey(),
                        createFormDataParts("key", "myDifferentFileName.txt"),
                        true));
    }
}
