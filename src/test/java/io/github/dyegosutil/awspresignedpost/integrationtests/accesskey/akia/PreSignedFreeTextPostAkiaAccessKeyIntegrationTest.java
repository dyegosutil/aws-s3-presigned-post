package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.akia;

import io.github.dyegosutil.awspresignedpost.TestUtils;
import io.github.dyegosutil.awspresignedpost.integrationtests.accesskey.PreSignedFreeTextPostCommonIntegrationTest;
import io.github.dyegosutil.awspresignedpost.postparams.FreeTextPostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedFreeTextPost;
import io.github.dyegosutil.awspresignedpost.signer.S3PostSigner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class PreSignedFreeTextPostAkiaAccessKeyIntegrationTest
        extends PreSignedFreeTextPostCommonIntegrationTest {

    private static final ZonedDateTime DATE = ZonedDateTime.now(Clock.systemUTC());

    @ParameterizedTest(name = "{0}")
    @MethodSource("freeTextPostParamsTestCases")
    void shouldTestFreeTextConditionSessionToken(
            String testDescription, Map<String, String> formDataParts) {
        assertThat(uploadToAws(formDataParts, getUrl())).isTrue();
    }

    private static Stream<Arguments> freeTextPostParamsTestCases() {
        return Stream.of(getSimpleUploadTestCase(), getEncryptionWithCustomerKeyTestCase());
    }

    private static Arguments getEncryptionWithCustomerKeyTestCase() {
        FreeTextPostParams freeTextPostParams =
                getFreeTextPostParams(getConditionsForUploadWithCustomerEncryptionKey());
        PreSignedFreeTextPost preSignedPost = S3PostSigner.sign(freeTextPostParams);
        return of(
                "Should upload file using free text post params where file encryption is used with"
                        + " key specified by the user. One of the most complex cases",
                getFormData(preSignedPost, getFormDataPartsForUploadWithCustomerEncryptionKey()));
    }

    /**
     * @return Simple test using mandatory params
     */
    private static Arguments getSimpleUploadTestCase() {
        FreeTextPostParams freeTextPostParams = getFreeTextPostParams(getMandatoryConditions());
        PreSignedFreeTextPost preSignedPost = S3PostSigner.sign(freeTextPostParams);
        return of(
                "Should upload file using free text post params where mandatory params are used."
                        + " This is the simplest upload condition possible",
                getFormData(preSignedPost, getMandatoryFormDataParts()));
    }

    private static Set<String[]> getMandatoryConditions() {
        Set<String[]> conditions = getCommonConditions();
        conditions.add(new String[] {"eq", "$x-amz-credential", getCredential()});
        return conditions;
    }

    private static Set<String[]> getConditionsForUploadWithCustomerEncryptionKey() {
        Set<String[]> conditions = getMandatoryConditions();
        conditions.add(
                new String[] {"eq", "$x-amz-server-side-encryption-customer-algorithm", "AES256"});
        conditions.add(
                new String[] {
                    "eq",
                    "$x-amz-server-side-encryption-customer-key",
                    encodeToBase64(encryptionKey256bits)
                });
        conditions.add(
                new String[] {
                    "eq",
                    "$x-amz-server-side-encryption-customer-key-MD5",
                    generateEncryptionKeyMD5DigestAsBase64()
                });
        return conditions;
    }

    private static Map<String, String> getMandatoryFormDataParts() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", TestUtils.getAmzDateFormatter().format(DATE));
        return formDataParts;
    }

    private static Map<String, String> getFormDataPartsForUploadWithCustomerEncryptionKey() {
        Map<String, String> formDataParts = new HashMap<>();
        formDataParts.put("key", "test.txt");
        formDataParts.put("x-amz-credential", getCredential());
        formDataParts.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        formDataParts.put("x-amz-date", TestUtils.getAmzDateFormatter().format(DATE));
        formDataParts.put("x-amz-server-side-encryption-customer-algorithm", "AES256");
        formDataParts.put(
                "x-amz-server-side-encryption-customer-key", encodeToBase64(encryptionKey256bits));
        formDataParts.put(
                "x-amz-server-side-encryption-customer-key-MD5",
                generateEncryptionKeyMD5DigestAsBase64());

        return formDataParts;
    }
}
