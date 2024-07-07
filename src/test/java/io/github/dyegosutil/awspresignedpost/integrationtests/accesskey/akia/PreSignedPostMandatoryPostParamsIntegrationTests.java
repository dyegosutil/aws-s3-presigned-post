package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.akia;

import static io.github.dyegosutil.awspresignedpost.conditions.KeyConditionUtil.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import io.github.dyegosutil.awspresignedpost.conditions.key.ExactKeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyStartingWithCondition;
import io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.IntegrationTests;
import io.github.dyegosutil.awspresignedpost.postparams.PostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedPost;
import io.github.dyegosutil.awspresignedpost.signer.S3PostSigner;

import okhttp3.Request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Stream;

public class PreSignedPostMandatoryPostParamsIntegrationTests extends IntegrationTests {

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_SESSION_TOKEN", null);
    }

    @Test
    void shouldUploadFileWithMandatoryParams() {
        // Arrange
        PostParams postParams =
                PostParams.builder(REGION, EXPIRATION_DATE, BUCKET, withKey("test.txt")).build();
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
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
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
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
                    region, expirationDate, bucket, (ExactKeyCondition) keyCondition);
        }
        if (keyCondition instanceof KeyStartingWithCondition) {
            return createPostParamsWithKeyStartingWithCondition(
                    region, expirationDate, bucket, (KeyStartingWithCondition) keyCondition);
        }
        throw new IllegalArgumentException(
                "Cannot create PostParams. Only ExactKeyCondition and KeyStartingWithCondition are"
                        + " supported");
    }

    private PostParams createPostParamsWithExactKeyCondition(
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            ExactKeyCondition exactKeyCondition) {
        return PostParams.builder(region, expirationDate, bucket, exactKeyCondition).build();
    }

    private PostParams createPostParamsWithKeyStartingWithCondition(
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyStartingWithCondition keyStartingWithCondition) {
        return PostParams.builder(region, expirationDate, bucket, keyStartingWithCondition).build();
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
                        Region.IL_CENTRAL_1,
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
                        true),
                // key using variable ${filename}
                of(
                        "Should succeed while uploading file to S3 when the ${filename} is used",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/box/"),
                        createFormDataParts("key", "user/leo/box/${filename}"),
                        true));
    }
}
