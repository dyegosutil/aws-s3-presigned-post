package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class SessionTokenIntegrationTests extends IntegrationTests {

    @Test
    @DisplayName("Should succeed while uploading file to S3 using the same session token added in the policy")
    void arrangeThatAwsSessionCredentialIsUsed_actUploadingTheFile_assertSuccess() {
        // Arrange
        PostParams postParams = createDefaultPostParamBuilder().build();
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProviderWithAwsSessionCredentials()).create(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(true);
    }

    @Test
    @DisplayName("Should fail while uploading file to S3 using a different session token added in the policy")
    void arrangeThatWrongAwsSessionCredentialIsUsed_actUploadingTheFile_assertSuccess() {
        // Arrange
        PostParams postParams = createDefaultPostParamBuilder().build();
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProviderWithAwsSessionCredentials()).create(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        conditions.put("x-amz-security-token",  "thisTokenIsWrong");
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(false);
    }
}
