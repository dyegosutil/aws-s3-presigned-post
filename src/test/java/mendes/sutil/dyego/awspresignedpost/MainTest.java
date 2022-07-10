package mendes.sutil.dyego.awspresignedpost;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

class MainTest {

    @Test
    public void test() {
        //        createPresignedPost
        //                PresignedPost.buider().withRegion(Region.EU_CENTRAL_1).build()
        PostParams postParams = PostParams
                .builder()
                .withKey("test_pre_signed_post.txt")
                .withRegion(Region.EU_CENTRAL_1)
                .withBucket("dyegosutil") // TODO double check what is mandatory
                //                                        .withExpiration(getTwoDaysInTheFuture())
                //                                        .withToken
                //                                        ("FwoGZXIvYXdzEAMaDJnjMcCzZ05MxE5udCKzAd8acj8V3dKJfJbtEASA07VbfGfsSsd5MXSC4PnsBr8q4VXbseNaV6IXIeAknFF0w4+Vcy/2q2krRqxXYhaQBKrTqj0f/622MlaS+DCQc6rJm0JxG9p0Ws3ftDWC89Nm85bRoFmNucBpVIr1eakuzFknTIqtm5PuLlYiis6ybiTRrUQ8kXmEjy8u5BjSORKScjMVy5WQmcfcxTIodyonRVbyGr6tJo4URs7Iu2CKJL6LQgURKJOa8pUGMi0Hcs2nE/IEApn7izSTyUmgfTgzNcGJsTbBeeJHM49RNOaCcI8IVkRlZlJFE28=")
                .build();


//        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsSessionCredentials.create("AKIAUNVUIU7WNNHBF5IU", "l0jOYwb6LGmKDsTI5T07fpHZngWVdWjd1XKtYXO8", ""));
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIAUNVUIU7WNNHBF5IU", "l0jOYwb6LGmKDsTI5T07fpHZngWVdWjd1XKtYXO8"));

        PresignedPost presignedPost = new S3PostSigner(credentialsProvider).createPresignedPost(postParams);
        System.out.println(presignedPost.getUrl());
        assert true;
    }

}