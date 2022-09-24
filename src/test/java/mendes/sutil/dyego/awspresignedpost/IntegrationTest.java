package mendes.sutil.dyego.awspresignedpost;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.KeyConditionHelper.withKey;
import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    @Disabled
    @Test
    public void test() {
        ZonedDateTime expirationDate = Instant.now(Clock.systemDefaultZone()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .plus(15, ChronoUnit.MINUTES)
                .atZone(ZoneId.systemDefault());

        PostParams postParams = PostParams
                .builder(Region.EU_CENTRAL_1, expirationDate, withKey("test.txt")) // TODO pass mandatory paramters here?
                .withBucket("dyegosutil") // TODO double check what is mandatory
                //                                        .withExpiration(getTwoDaysInTheFuture())
                //                                        .withToken
                //                                        ("FwoGZXIvYXdzEAMaDJnjMcCzZ05MxE5udCKzAd8acj8V3dKJfJbtEASA07VbfGfsSsd5MXSC4PnsBr8q4VXbseNaV6IXIeAknFF0w4+Vcy/2q2krRqxXYhaQBKrTqj0f/622MlaS+DCQc6rJm0JxG9p0Ws3ftDWC89Nm85bRoFmNucBpVIr1eakuzFknTIqtm5PuLlYiis6ybiTRrUQ8kXmEjy8u5BjSORKScjMVy5WQmcfcxTIodyonRVbyGr6tJo4URs7Iu2CKJL6LQgURKJOa8pUGMi0Hcs2nE/IEApn7izSTyUmgfTgzNcGJsTbBeeJHM49RNOaCcI8IVkRlZlJFE28=")
                .build();

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_KEY"), System.getenv("AWS_SECRET")));

        PresignedPost presignedPost = new S3PostSigner(credentialsProvider).create(postParams);
        System.out.println(presignedPost);

        Boolean wasUploadSuccessful = false;
        try {
            wasUploadSuccessful = uploadToAws(presignedPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(wasUploadSuccessful).isTrue();
    }

    /**
     * TODO Change to a better http client since okhttp does not give as much information as postman when a 400 happens.
     * If errors happens here, better debug with postman
     *
     * @param presignedPost
     * @return
     * @throws IOException In case the upload is not successful
     */
    private boolean uploadToAws(PresignedPost presignedPost) throws IOException {
        final OkHttpClient client = new OkHttpClient();
        String fileContent = "this is a testdy";

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(presignedPost.getAlgorithm().getKey(), presignedPost.getAlgorithm().getValue())
                .addFormDataPart("key", "${filename}")
                .addFormDataPart(presignedPost.getCredential().getKey(), presignedPost.getCredential().getValue())
                .addFormDataPart(presignedPost.getXAmzSignature().getKey(), presignedPost.getXAmzSignature().getValue()) // TODO fix this
                .addFormDataPart(presignedPost.getDate().getKey(), presignedPost.getDate().getValue())
                .addFormDataPart(presignedPost.getPolicy().getKey(), presignedPost.getPolicy().getValue())
                .addFormDataPart("file", "test.txt", RequestBody.create(fileContent.getBytes(), MediaType.parse("text/plain")))
                .build();

        Request request = new Request.Builder().url(presignedPost.getUrl()).post(formBody).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response + response.message());
            } else {
                return true;
            }
        }
    }
}