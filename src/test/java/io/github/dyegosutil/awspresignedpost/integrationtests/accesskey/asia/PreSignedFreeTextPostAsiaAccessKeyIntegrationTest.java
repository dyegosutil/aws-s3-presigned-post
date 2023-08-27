package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.asia;

import io.github.dyegosutil.awspresignedpost.TestUtils;
import io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.PreSignedFreeTextPostCommonIntegrationTest;
import io.github.dyegosutil.awspresignedpost.postparams.FreeTextPostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedFreeTextPost;
import io.github.dyegosutil.awspresignedpost.signer.S3PostSigner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * To run this test the following environment variables must be set: AWS_ACCESS_KEY_ID (ASIA...),
 * AWS_BUCKET, AWS_REGION, AWS_SECRET_ACCESS_KEY and AWS_SESSION_TOKEN
 */
@Disabled
public class PreSignedFreeTextPostAsiaAccessKeyIntegrationTest
        extends PreSignedFreeTextPostCommonIntegrationTest {

    @Test
    @DisplayName("Should upload file using free text post params and short term credentials")
    void freeTextStsTokenTest() {
        // Arrange
        FreeTextPostParams freeTextPostParams = getFreeTextPostParams(getConditionsForAsiaKey());
        PreSignedFreeTextPost preSignedPost = S3PostSigner.sign(freeTextPostParams);
        Map<String, String> formDataParts = getFormData(preSignedPost, getFormDataPartsAwsSts());

        // Act & Assert
        assertThat(uploadToAws(formDataParts, getUrl())).isTrue();
    }

    private static Set<String[]> getConditionsForAsiaKey() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(new String[] {"eq", "$x-amz-credential", getCredential()});
        conditions.add(
                new String[] {"eq", "$x-amz-security-token", System.getenv("AWS_SESSION_TOKEN")});
        return conditions;
    }

    private static Map<String, String> getFormDataPartsAwsSts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", TestUtils.getAmzDateFormatter().format(DATE));
        formDataParts.put("x-amz-security-token", System.getenv("AWS_SESSION_TOKEN"));
        return formDataParts;
    }
}
