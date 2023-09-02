package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.akia;

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

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class PreSignedPostWithSuccessActionStatusPostParamIntegrationTests
        extends IntegrationTests {

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_SESSION_TOKEN", null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesForWithSuccessActionStatus")
    void shouldUploadFileUsingSuccessActionStatus(
            String testDescription, PostParams postParams, int expectedResponseCode) {
        // Arrange
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        int responseCode = postFileIntoS3ReturningSuccessActionStatus(request);

        // Arrange
        assertThat(responseCode).isEqualTo(expectedResponseCode);
    }

    @Test
    void shouldNotUploadFileUsingWrongSuccessActionStatus() {
        // Arrange
        PostParams postParams =
                createDefaultPostParamBuilderSpecifyingKey()
                        .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.OK)
                        .build();
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
        Map<String, String> conditions = presignedPost.getConditions();
        conditions.put("success_action_status", "299");
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        int responseCode = postFileIntoS3ReturningSuccessActionStatus(request);

        // Assert
        assertThat(responseCode).isEqualTo(403);
    }

    public static Stream<Arguments> getTestCasesForWithSuccessActionStatus() {
        return Stream.of(
                of(
                        "Should succeed while uploading file to S3 when the success_action_status"
                                + " specified is the same as the one in the policy and status code"
                                + " returned in the response should be 200",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.OK)
                                .build(),
                        200),
                of(
                        "Should succeed while uploading file to S3 when the success_action_status"
                                + " specified is the same as the one in the policy and status code"
                                + " returned in the response should be 201",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withSuccessActionStatus(
                                        PostParams.Builder.SuccessActionStatus.CREATED)
                                .build(),
                        201),
                of(
                        "Should succeed while uploading file to S3 when the success_action_status"
                                + " specified is the same as the one in the policy and status code"
                                + " returned in the response should be 204",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withSuccessActionStatus(
                                        PostParams.Builder.SuccessActionStatus.NO_CONTENT)
                                .build(),
                        204));
    }
}
