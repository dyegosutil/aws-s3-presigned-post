package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static mendes.sutil.dyego.awspresignedpost.conditions.KeyConditionHelper.withAnyKey;

public class TestUtils {

  public static final ZonedDateTime EXPIRATION_DATE =
      Instant.now(Clock.systemUTC()).plus(1, ChronoUnit.MINUTES).atZone(ZoneOffset.UTC);

  /**
   * @return The AwsCredentialsProvider to be used to create the pre-signed post
   */
  public static AwsCredentialsProvider getAmazonCredentialsProvider() {
    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(System.getenv("AWS_KEY"), System.getenv("AWS_SECRET")));
  }

  public static AwsCredentialsProvider getAmazonCredentialsProvider(
      String awsKey, String awsSecret) {
    return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsKey, awsSecret));
  }

  public static AwsCredentialsProvider getAmazonCredentialsProviderWithAwsSessionCredentials() {
    return StaticCredentialsProvider.create(
        AwsSessionCredentials.create(
            System.getenv("AWS_SESSION_KEY"),
            System.getenv("AWS_SESSION_SECRET"),
            System.getenv("AWS_SESSION_TOKEN")));
  }

  public static PostParams createPostParamsWithKeyStartingWith() {
    return PostParams.builder(Region.AP_EAST_1, EXPIRATION_DATE, "myBucket", withAnyKey()).build();
  }

  public static DateTimeFormatter getAmzDateFormatter() {
    return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
        .withZone(ZoneOffset.UTC);
  }

  public static DateTimeFormatter getYyyyMmDdDateFormatter() {
    return DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH).withZone(ZoneOffset.UTC);
  }
}
