package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.presigned.PreSignedPost;
import mendes.sutil.dyego.awspresignedpost.signer.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.getAmazonCredentialsProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class WithSuccessActionStatusPostParamIntegrationTests extends IntegrationTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesForWithSuccessActionStatus")
    void shouldUploadFileUsingSuccessActionStatus(
            String testDescription, PostParams postParams, int expectedResponseCode) {
        // Arrange
        PreSignedPost presignedPost =
                S3PostSigner.sign(postParams, getAmazonCredentialsProvider());

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
        PreSignedPost presignedPost =
                S3PostSigner.sign(postParams, getAmazonCredentialsProvider());
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
