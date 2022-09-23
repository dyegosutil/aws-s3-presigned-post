package mendes.sutil.dyego.awspresignedpost;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

class MainTest {

    @Test
    public void test() {
        ZonedDateTime expirationDate = Instant.now(Clock.systemDefaultZone()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .plus(15, ChronoUnit.MINUTES)
                .atZone(ZoneId.systemDefault());

        PostParams postParams = PostParams
                .builder(expirationDate) // TODO pass mandatory paramters here?
                .withKey("pira2.txt")
                .withRegion(Region.EU_CENTRAL_1)
                .withBucket("dyegosutil") // TODO double check what is mandatory
                //                                        .withExpiration(getTwoDaysInTheFuture())
                //                                        .withToken
                //                                        ("FwoGZXIvYXdzEAMaDJnjMcCzZ05MxE5udCKzAd8acj8V3dKJfJbtEASA07VbfGfsSsd5MXSC4PnsBr8q4VXbseNaV6IXIeAknFF0w4+Vcy/2q2krRqxXYhaQBKrTqj0f/622MlaS+DCQc6rJm0JxG9p0Ws3ftDWC89Nm85bRoFmNucBpVIr1eakuzFknTIqtm5PuLlYiis6ybiTRrUQ8kXmEjy8u5BjSORKScjMVy5WQmcfcxTIodyonRVbyGr6tJo4URs7Iu2CKJL6LQgURKJOa8pUGMi0Hcs2nE/IEApn7izSTyUmgfTgzNcGJsTbBeeJHM49RNOaCcI8IVkRlZlJFE28=")
                .build();

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_KEY"), System.getenv("AWS_SECRET")));

        PresignedPost presignedPost = new S3PostSigner(credentialsProvider).create(postParams);
        System.out.println(presignedPost);
        assert true;
    }

}