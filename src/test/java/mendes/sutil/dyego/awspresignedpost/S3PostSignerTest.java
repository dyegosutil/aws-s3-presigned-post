package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.result.PresignedPost;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.*;
import static mendes.sutil.dyego.awspresignedpost.conditions.helper.KeyConditionHelper.withAnyKey;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class S3PostSignerTest {

    public static final String ERROR_MESSAGE_NULL_AWS_CREDENTIALS = "AwsCredentialsProvider must provide non-null AwsCredentials";

    @Test
    void fullTest() {
        String bucket = "myBucket";
        Region region = Region.EU_CENTRAL_1;
        String awsKey = "AKIAIOSFODNN7EXAMPLE";
        PresignedPost presignedPost = S3PostSigner.create(
                PostParams.builder(
                        region,
                        EXPIRATION_DATE,
                        "myBucket",
                        withAnyKey()
                ).build(),
                getAmazonCredentialsProvider(
                        awsKey,
                        "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
                )
        );
        Map<String, String> actualConditions = presignedPost.getConditions();
        assertThat(actualConditions.size()).isEqualTo(6);
        assertThat(actualConditions).extractingByKey("policy", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-signature", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-date", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-algorithm", as(STRING)).isEqualTo("AWS4-HMAC-SHA256");
        assertThat(actualConditions).extractingByKey("x-amz-credential", as(STRING))
                .isEqualTo(awsKey + "/" + getCredentialDate() + "/" + region + "/s3/aws4_request");
        assertThat(actualConditions).containsEntry("key", "${filename}");
        assertThat(presignedPost.getUrl()).isEqualTo("https://" + bucket + ".s3." + region + ".amazonaws.com");
    }

    private String getCredentialDate() {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC)
                .format(ZonedDateTime.now());
    }

    @Test
    void shouldValidateAwsCredentialsForPreSignedPost() {
        assertThatThrownBy(() -> S3PostSigner.create(
                createPostParamsWithKeyStartingWith(),
                mockAwsCredentialsProvider()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ERROR_MESSAGE_NULL_AWS_CREDENTIALS);
    }

    @Test
    void shouldValidateAwsCredentialsForFreeTextPreSignedPost() {
        assertThatThrownBy(() -> S3PostSigner.create(
                createFreeTextPostParams(),
                mockAwsCredentialsProvider()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ERROR_MESSAGE_NULL_AWS_CREDENTIALS);
    }

    private AwsCredentialsProvider mockAwsCredentialsProvider() {
        AwsCredentialsProvider mock = mock(AwsCredentialsProvider.class);
        when((mock).resolveCredentials()).thenReturn(null);
        return mock;
    }
}