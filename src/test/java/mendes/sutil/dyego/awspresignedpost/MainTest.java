package mendes.sutil.dyego.awspresignedpost;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

class MainTest {

    @Test
    public void test() {

        System.out.println();
        //        createPresignedPost
        //                PresignedPost.buider().withRegion(Region.EU_CENTRAL_1).build()
        PostRequestData postRequestData = PostRequestData
                .builder()
                .withKey("test_pre_signed_post.txt")
                .withRegion(Region.EU_CENTRAL_1)
                //            .withEndpoint("https://s3.eu-central-1.amazonaws.com/dev-de.upload-user-documents.scalable")
                //                                        .withBucket("dev-de.upload-user-documents.scalable")
                //                                        .withExpiration(getTwoDaysInTheFuture())
                //                                        .withToken
                //                                        ("FwoGZXIvYXdzEAMaDJnjMcCzZ05MxE5udCKzAd8acj8V3dKJfJbtEASA07VbfGfsSsd5MXSC4PnsBr8q4VXbseNaV6IXIeAknFF0w4+Vcy/2q2krRqxXYhaQBKrTqj0f/622MlaS+DCQc6rJm0JxG9p0Ws3ftDWC89Nm85bRoFmNucBpVIr1eakuzFknTIqtm5PuLlYiis6ybiTRrUQ8kXmEjy8u5BjSORKScjMVy5WQmcfcxTIodyonRVbyGr6tJo4URs7Iu2CKJL6LQgURKJOa8pUGMi0Hcs2nE/IEApn7izSTyUmgfTgzNcGJsTbBeeJHM49RNOaCcI8IVkRlZlJFE28=")
                .build();


        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsSessionCredentials.create("test", "test", ""));
        new S3PostSigner(credentialsProvider).createPresignedPost(postRequestData);
        assert true;
    }

}