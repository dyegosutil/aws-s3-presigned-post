package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.result.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.getAmazonCredentialsProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class WithSuccessActionRedirectPostParamIntegrationTests extends IntegrationTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource("getRedirectUploadConditionsTestCases")
    void arrangeThatRedirectIsUsed_actUploadingTheFile_assertSuccess(
            String testDescription,
            PostParams postParams,
            String redirectAwsConditionName
    ) {
        // Act
        PresignedPost presignedPost = S3PostSigner.create(postParams, getAmazonCredentialsProvider());

        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        String redirectInResponse = postFileIntoS3ReturningRedirect(request);

        // Arrange
        assertThat(redirectInResponse).isEqualTo(conditions.get(redirectAwsConditionName));
    }

    /**
     * When the upload is not successful, the redirect in the response is in the following format
     * https://bucket.s3.region.amazonaws.com
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomizedRedirectUploadConditionsTestCases")
    void arrangeThatCustomizedRedirectIsUsed_actUploadingTheFile_assertExpectedResult(
            String testDescription,
            PostParams postParams,
            Map<String, String> formDataParts,
            String redirectAwsConditionName
    ) {
        // Arrange
        PresignedPost presignedPost = S3PostSigner.create(postParams, getAmazonCredentialsProvider());
        Map<String, String> conditions = presignedPost.getConditions();
        conditions.putAll(formDataParts);
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        String redirectInResponse = postFileIntoS3ReturningRedirect(request);

        // Assert
        assertThat(redirectInResponse).isEqualTo(conditions.get(redirectAwsConditionName));
    }

    public static Stream<Arguments> getRedirectUploadConditionsTestCases() {
        return Stream.of(
                // success_action_redirect
                of(
                        "Should succeed while uploading file to S3 when using the same " +
                                "success_action_redirect specified in the policy and having the correct return from the " +
                                "http client",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withSuccessActionRedirect("https://www.google.com")
                                .build(),
                        "success_action_redirect"
                ),
                // redirect
                of(
                        "Should succeed while uploading file to S3 when using the same " +
                                "redirect specified in the policy and having the correct return from the " +
                                "http client",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withRedirect("https://www.google.com")
                                .build(),
                        "redirect"
                )
        );
    }

    public static Stream<Arguments> getCustomizedRedirectUploadConditionsTestCases() {
        return Stream.of(
                // success_action_redirect
                of(
                        "Should fail while uploading file to S3 when using a different " +
                                "success_action_redirect specified in the policy and having the unsuccessful return " +
                                "from the http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirect("https://www.google.com")
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_redirect", String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "success_action_redirect"
                ),
                // success_action_redirect
                of(
                        "Should succeed while uploading file to S3 when using the same initial string " +
                                "success_action_redirect specified in the policy and having the correct return from the " +
                                "http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirectStartingWith("https://www.google.")
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_redirect", "https://www.google.com.br"),
                        "success_action_redirect"
                ),
                // success_action_redirect
                of(
                        "Should fail while uploading file to S3 when using a different initial string" +
                                "success_action_redirect than specified in the policy and having the unsuccessful return " +
                                "from the http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirectStartingWith("https://www.google")
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_redirect", String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "success_action_redirect"
                ),
                // redirect
                of(
                        "Should fail while uploading file to S3 when not using the same " +
                                "redirect specified in the policy and having the correct return from the " +
                                "http client",
                        createDefaultPostParamBuilder()
                                .withRedirect("https://www.google.com")
                                .build(),
                        createFormDataPartsWithKeyCondition("redirect", String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "redirect"
                ),
                // redirect
                of(
                        "Should fail while uploading file to S3 when using a different initial string" +
                                "redirect than specified in the policy and having the unsuccessful return " +
                                "from the http client",
                        createDefaultPostParamBuilder()
                                .withRedirectStartingWith("https://www.google")
                                .build(),
                        createFormDataPartsWithKeyCondition("redirect", String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "redirect"
                ),
                // redirect
                of(
                        "Should succeed while uploading file to S3 when using the same initial string" +
                                "redirect than specified in the policy and having the unsuccessful return " +
                                "from the http client",
                        createDefaultPostParamBuilder()
                                .withRedirectStartingWith("https://www.google")
                                .build(),
                        createFormDataPartsWithKeyCondition("redirect", "https://www.google.com"),
                        "redirect"
                )
        );
    }
}
