package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.akia;

import io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.IntegrationTests;
import io.github.dyegosutil.awspresignedpost.postparams.PostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedPost;
import io.github.dyegosutil.awspresignedpost.signer.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class PreSignedPostWithSuccessActionRedirectPostParamIntegrationTests
        extends IntegrationTests {

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_SESSION_TOKEN", null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getRedirectUploadConditionsTestCases")
    void shouldTestUploadUsingRedirectCondition(
            String testDescription, PostParams postParams, String redirectAwsConditionName) {
        // Act
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);

        Map<String, String> conditions = presignedPost.getConditions();
        Request request = createRequestFromConditions(conditions, presignedPost.getUrl());

        // Act
        String redirectInResponse = postFileIntoS3ReturningRedirect(request);

        // Arrange
        assertThat(redirectInResponse).isEqualTo(conditions.get(redirectAwsConditionName));
    }

    /**
     * When the upload is not successful, the redirect in the response is in the following format
     * [bucket].s3.region.amazonaws.com
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomizedRedirectUploadConditionsTestCases")
    void shouldTestUploadUsingCustomizedRedirectCondition(
            String testDescription,
            PostParams postParams,
            Map<String, String> formDataParts,
            String redirectAwsConditionName) {
        // Arrange
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
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
                        "Should succeed while uploading file to S3 when using the same"
                                + " success_action_redirect specified in the policy and having the"
                                + " correct return from the http client",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withSuccessActionRedirect("https://www.google.com")
                                .build(),
                        "success_action_redirect"));
    }

    public static Stream<Arguments> getCustomizedRedirectUploadConditionsTestCases() {
        return Stream.of(
                // success_action_redirect
                of(
                        "Should fail while uploading file to S3 when using a different"
                                + " success_action_redirect specified in the policy and having the"
                                + " unsuccessful return from the http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirect("https://www.google.com")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "success_action_redirect",
                                String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "success_action_redirect"),
                // success_action_redirect
                of(
                        "Should succeed while uploading file to S3 when using the same initial"
                                + " string success_action_redirect specified in the policy and having"
                                + " the correct return from the http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirectStartingWith("https://www.google.")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "success_action_redirect", "https://www.google.com.br"),
                        "success_action_redirect"),
                // success_action_redirect
                of(
                        "Should fail while uploading file to S3 when using a different initial"
                                + " stringsuccess_action_redirect than specified in the policy and"
                                + " having the unsuccessful return from the http client",
                        createDefaultPostParamBuilder()
                                .withSuccessActionRedirectStartingWith("https://www.google")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "success_action_redirect",
                                String.format("https://%s.s3.eu-central-1.amazonaws.com", BUCKET)),
                        "success_action_redirect"));
    }
}
