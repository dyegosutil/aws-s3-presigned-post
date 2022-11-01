package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class SessionTokenIntegrationTests extends IntegrationTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesWithAwsSessionCredentials")
    void testWithAwsSessionCredentials(
            String testDescription,
            PostParams postParams,
            Map<String, String> formDataParts,
            Boolean expectedResult
    ) {
        // Arrange
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProviderWithAwsSessionCredentials()).create(postParams);
        System.out.println(presignedPost); // TODO

        // Act
        Map<String, String> completeFormDataParts = fillFormData(presignedPost, formDataParts);
        Boolean wasUploadSuccessful = uploadToAws(presignedPost, completeFormDataParts);

        // Assert
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    public static Stream<Arguments> getTestCasesWithAwsSessionCredentials() {
        return Stream.of(
                of(
                        "Should succeed while uploading file to S3 using the same session token added in the policy",
                        createDefaultPostParamBuilder()
                                .build(),
                        null,
                        true
                ),
                of(
                        "Should fail while uploading file to S3 using a different session token added in the policy",
                        createDefaultPostParamBuilder()
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-security-token",  "thisTokenIsWrong"),
                        false
                )
        );
    }

}
