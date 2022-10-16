package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.PresignedPost;
import mendes.sutil.dyego.awspresignedpost.S3PostSigner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class WithSuccessActionStatusPostParamIntegrationTests extends IntegrationTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesForWithSuccessActionStatus")
    void testWithSuccessActionStatus(
            String testDescription,
            PostParams postParams,
            Map<String, String> formDataParts,
            int expectedResponseCode,
            Boolean expectedResult
    ) {
        PresignedPost presignedPost = new S3PostSigner(getAmazonCredentialsProvider()).create(postParams);
        System.out.println(presignedPost);
        Boolean wasUploadSuccessful = uploadToAwsCheckingSuccessActionStatus(presignedPost, formDataParts, expectedResponseCode);
        assertThat(wasUploadSuccessful).isEqualTo(expectedResult);
    }

    public static Stream<Arguments> getTestCasesForWithSuccessActionStatus() {
        return Stream.of(
                of(
                        "Should succeed while uploading file to S3 when the success_action_status specified is " +
                                "the same as the one in the policy and status code returned in the response should be 200",
                        createDefaultPostParamBuilder()
                                .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.OK)
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_status", "200"),
                        200,
                        true
                ),
                of(
                        "Should succeed while uploading file to S3 when the success_action_status specified is " +
                                "the same as the one in the policy and status code returned in the response should be 201",
                        createDefaultPostParamBuilder()
                                .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.CREATED)
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_status", "201"),
                        201,
                        true
                ),
                of(
                        "Should succeed while uploading file to S3 when the success_action_status specified is " +
                                "the same as the one in the policy and status code returned in the response should be 204",
                        createDefaultPostParamBuilder()
                                .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.NO_CONTENT)
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_status", "204"),
                        204,
                        true
                ),
                of(
                        "Should fail while uploading file to S3 when the success_action_status specified is " +
                                "different than the one in the policy and status code returned in the response should be 403",
                        createDefaultPostParamBuilder()
                                .withSuccessActionStatus(PostParams.Builder.SuccessActionStatus.OK)
                                .build(),
                        createFormDataPartsWithKeyCondition("success_action_status", "299"),
                        403,
                        false
                )
        );
    }
}
