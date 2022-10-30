package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;


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
        Boolean wasUploadSuccessful = uploadToAws(presignedPost, formDataParts);

        // Assert
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    public static Stream<Arguments> getTestCasesWithAwsSessionCredentials() {
        return Stream.of(
                of(
                        "Should succeed while uploading file to S3 using the same session token added in the policy",
                        createDefaultPostParamBuilder()
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-security-token",  System.getenv("AWS_SESSION_TOKEN")),
                        true
                )
//                ,
//                of(
//                        "Should fail while uploading file to S3 using a different session token added in the policy",
//                        createDefaultPostParamBuilder()
//                                .build(),
//                        createFormDataPartsWithKeyCondition("x-amz-security-token",  "thisTokenIsWrong"),
//                        false
//                )
        );
    }

}
