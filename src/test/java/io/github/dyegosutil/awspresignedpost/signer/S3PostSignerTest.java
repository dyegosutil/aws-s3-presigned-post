package io.github.dyegosutil.awspresignedpost.signer;

import io.github.dyegosutil.awspresignedpost.TestUtils;
import io.github.dyegosutil.awspresignedpost.postparams.FreeTextPostParams;
import io.github.dyegosutil.awspresignedpost.postparams.PostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedFreeTextPost;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedPost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.github.dyegosutil.awspresignedpost.conditions.KeyConditionUtil.withAnyKey;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@ExtendWith(SystemStubsExtension.class)
public class S3PostSignerTest {

    public static final Region REGION = Region.EU_CENTRAL_1;
    public static final String AWS_FAKE_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    public static final String AWS_FAKE_KEY = "AKIAIOSFODNN7EXAMPLE";

    @SystemStub private EnvironmentVariables environmentVariables;

    @Test
    void shouldReturnCorrectPreSignedPost() {
        // Arrange
        String bucket = "myBucket";
        environmentVariables.set("AWS_ACCESS_KEY_ID", AWS_FAKE_KEY);
        environmentVariables.set("AWS_SECRET_ACCESS_KEY", AWS_FAKE_SECRET);
        environmentVariables.set("AWS_SESSION_TOKEN", null);

        // Act
        PreSignedPost presignedPost =
                S3PostSigner.sign(
                        PostParams.builder(REGION, TestUtils.EXPIRATION_DATE, bucket, withAnyKey())
                                .build());

        // Assert
        Map<String, String> actualConditions = presignedPost.getConditions();
        assertThat(actualConditions.size()).isEqualTo(6);
        assertThat(actualConditions).extractingByKey("policy", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-signature", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-date", as(STRING)).isNotEmpty();
        assertThat(actualConditions)
                .extractingByKey("x-amz-algorithm", as(STRING))
                .isEqualTo("AWS4-HMAC-SHA256");
        assertThat(actualConditions)
                .extractingByKey("x-amz-credential", as(STRING))
                .isEqualTo(
                        AWS_FAKE_KEY
                                + "/"
                                + getCredentialDate()
                                + "/"
                                + REGION
                                + "/s3/aws4_request");
        assertThat(actualConditions).containsEntry("key", "");
        assertThat(presignedPost.getUrl())
                .isEqualTo("https://" + bucket + ".s3." + REGION + ".amazonaws.com");
    }

    @Test
    void shouldReturnCorrectFreeTextPreSignedPost() {
        // Arrange
        environmentVariables.set("AWS_ACCESS_KEY_ID", AWS_FAKE_KEY);
        environmentVariables.set("AWS_SECRET_ACCESS_KEY", AWS_FAKE_SECRET);
        environmentVariables.set("AWS_SESSION_TOKEN", "fakeSessionToken");
        ZonedDateTime date = ZonedDateTime.of(2022, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC);
        ZonedDateTime expirationDate = Instant.parse("2900-01-01T10:15:30Z").atZone(ZoneOffset.UTC);
        Region region = Region.EU_CENTRAL_1;
        Set<String[]> conditions = new HashSet<>();
        conditions.add(new String[] {"eq", "$key", "test.txt"});
        conditions.add(new String[] {"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"});
        conditions.add(
                new String[] {"eq", "$x-amz-date", TestUtils.getAmzDateFormatter().format(date)});
        conditions.add(new String[] {"eq", "$bucket", "myBucket"});
        conditions.add(
                new String[] {
                    "eq",
                    "$x-amz-credential",
                    AWS_FAKE_KEY
                            + "/"
                            + TestUtils.getYyyyMmDdDateFormatter().format(date)
                            + "/"
                            + region
                            + "/s3/aws4_request"
                });
        FreeTextPostParams freeTextPostParams =
                new FreeTextPostParams(region, expirationDate, date, conditions);

        // Act
        PreSignedFreeTextPost preSignedPost = S3PostSigner.sign(freeTextPostParams);

        // Assert
        assertThat(preSignedPost.getPolicy()).isNotEmpty();
        assertThat(preSignedPost.getxAmzSignature()).isNotEmpty();
    }

    private String getCredentialDate() {
        return DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC)
                .format(ZonedDateTime.now());
    }

    @Test
    void shouldThrowSdkClientExceptionIfNoCredentialsForPreSignedPost() {
        // Arrange
        setInvalidAwsCredentials();

        // Act & Assert
        assertThatThrownBy(() -> S3PostSigner.sign(TestUtils.createPostParamsWithKeyStartingWith()))
                .isInstanceOf(SdkClientException.class);
    }

    @Test
    void shouldThrowSdkClientExceptionIfNoCredentialsForFreeTextPreSignedPost() {
        // Arrange
        setInvalidAwsCredentials();
        assertThatThrownBy(() -> S3PostSigner.sign(createFreeTextPostParams()))
                .isInstanceOf(SdkClientException.class);
    }

    private void setInvalidAwsCredentials() {
        environmentVariables.set("AWS_PROFILE", "wrong_profile");
        environmentVariables.set("AWS_ACCESS_KEY_ID", null);
        environmentVariables.set("AWS_SECRET_ACCESS_KEY", null);
        environmentVariables.set("AWS_SESSION_TOKEN", null);
    }

    public FreeTextPostParams createFreeTextPostParams() {
        return new FreeTextPostParams(
                Region.AP_EAST_1,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                Collections.singleton(new String[] {"eq", "$bucket", "myBucket"}));
    }
}
