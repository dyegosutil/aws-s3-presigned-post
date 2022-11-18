package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.postparams.FreeTextPostParams;
import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.result.FreeTextPresignedPost;
import mendes.sutil.dyego.awspresignedpost.result.PresignedPost;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static mendes.sutil.dyego.awspresignedpost.TestUtils.*;
import static mendes.sutil.dyego.awspresignedpost.conditions.helper.KeyConditionHelper.withAnyKey;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3PostSignerTest {

    public static final String ERROR_MESSAGE_NULL_AWS_CREDENTIALS = "AwsCredentialsProvider must provide non-null AwsCredentials";
    public static final Region REGION = Region.EU_CENTRAL_1;
    public static final String AWS_FAKE_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    public static final String AWS_FAKE_KEY = "AKIAIOSFODNN7EXAMPLE";

    @Test
    void shouldReturnCorrectPreSignedPost() {
        // Arrange
        String bucket = "myBucket";

        // Act
        PresignedPost presignedPost = S3PostSigner.create(
                PostParams.builder(
                        REGION,
                        EXPIRATION_DATE,
                        bucket,
                        withAnyKey()
                ).build(),
                getAmazonCredentialsProvider(
                        AWS_FAKE_KEY,
                        AWS_FAKE_SECRET
                )
        );

        // Assert
        Map<String, String> actualConditions = presignedPost.getConditions();
        assertThat(actualConditions.size()).isEqualTo(6);
        assertThat(actualConditions).extractingByKey("policy", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-signature", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-date", as(STRING)).isNotEmpty();
        assertThat(actualConditions).extractingByKey("x-amz-algorithm", as(STRING)).isEqualTo("AWS4-HMAC-SHA256");
        assertThat(actualConditions).extractingByKey("x-amz-credential", as(STRING))
                .isEqualTo(AWS_FAKE_KEY + "/" + getCredentialDate() + "/" + REGION + "/s3/aws4_request");
        assertThat(actualConditions).containsEntry("key", "");
        assertThat(presignedPost.getUrl()).isEqualTo("https://" + bucket + ".s3." + REGION + ".amazonaws.com");
    }

    @Test
    void shouldReturnCorrectFreeTextPreSignedPost() {
        // Arrange
        ZonedDateTime date = ZonedDateTime.of(2022, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC);
        ZonedDateTime expirationDate = Instant
                .parse("2900-01-01T10:15:30Z")
                .atZone(ZoneOffset.UTC);
        Region region = Region.EU_CENTRAL_1;
        Set<String[]> conditions = new HashSet<>();
        conditions.add(new String[]{"eq", "$key", "test.txt"});
        conditions.add(new String[]{"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"});
        conditions.add(new String[]{"eq", "$x-amz-date", getAmzDateFormatter().format(date)});
        conditions.add(new String[]{"eq", "$bucket", "myBucket"});
        conditions.add(new String[]{
                "eq",
                "$x-amz-credential",
                AWS_FAKE_KEY + "/" + getYyyyMmDdDateFormatter().format(date) + "/" + region + "/s3/aws4_request"
        });
        FreeTextPostParams freeTextPostParams = new FreeTextPostParams(
                region,
                expirationDate,
                date,
                conditions
        );

        // Act
        FreeTextPresignedPost preSignedPost = S3PostSigner.create(
                freeTextPostParams,
                getAmazonCredentialsProvider(AWS_FAKE_KEY, AWS_FAKE_SECRET)
        );

        // Assert
        assertThat(preSignedPost.getPolicy()).isNotEmpty();
        assertThat(preSignedPost.getxAmzSignature()).isNotEmpty();
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

    public FreeTextPostParams createFreeTextPostParams() {
        return new FreeTextPostParams(
                Region.AP_EAST_1,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                Collections.singleton(new String[]{"eq", "$bucket", "myBucket"})
        );
    }

    private AwsCredentialsProvider mockAwsCredentialsProvider() {
        AwsCredentialsProvider mock = mock(AwsCredentialsProvider.class);
        when((mock).resolveCredentials()).thenReturn(null);
        return mock;
    }
}