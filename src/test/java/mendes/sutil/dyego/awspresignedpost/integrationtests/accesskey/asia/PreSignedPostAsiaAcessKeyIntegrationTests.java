package mendes.sutil.dyego.awspresignedpost.integrationtests.accesskey.asia;

import mendes.sutil.dyego.awspresignedpost.integrationtests.accesskey.IntegrationTests;
import mendes.sutil.dyego.awspresignedpost.presigned.PreSignedPost;
import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.signer.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * To run this test the following environment variables must be set: AWS_ACCESS_KEY_ID (ASIA...), AWS_BUCKET,
 * AWS_REGION, AWS_SECRET_ACCESS_KEY and AWS_SESSION_TOKEN
 */
@Disabled
public class PreSignedPostAsiaAcessKeyIntegrationTests extends IntegrationTests {

    @Test
    @DisplayName(
            "Should succeed while uploading file to S3 using the same session token added in the"
                    + " policy")
    void shouldUploadFileWithSessionToken() {
        // Arrange
        PostParams postParams = createDefaultPostParamBuilderSpecifyingKey().build();
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(true);
    }

    @Test
    @DisplayName(
            "Should fail while uploading file to S3 using a different session token added in the"
                    + " policy")
    void shouldNotUploadFileWithWrongSessionToken() {
        // Arrange
        PostParams postParams = createDefaultPostParamBuilder().build();
        PreSignedPost presignedPost =
                S3PostSigner.sign(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        conditions.put("x-amz-security-token", "thisTokenIsWrong");
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(false);
    }
}
