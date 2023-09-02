package io.github.dyegosutil.awspresignedpost.integrationtests.accesskey;

import io.github.dyegosutil.awspresignedpost.TestUtils;
import io.github.dyegosutil.awspresignedpost.postparams.FreeTextPostParams;
import io.github.dyegosutil.awspresignedpost.presigned.PreSignedFreeTextPost;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PreSignedFreeTextPostCommonIntegrationTest extends IntegrationTests {

    protected static final ZonedDateTime DATE = ZonedDateTime.now(Clock.systemUTC());
    private static final DateTimeFormatter YYYYMMDD_DATE_FORMATTER =
            TestUtils.getYyyyMmDdDateFormatter();

    protected static Map<String, String> getFormData(
            PreSignedFreeTextPost preSignedPost, Map<String, String> formDataParts) {
        formDataParts.put("x-amz-signature", preSignedPost.getxAmzSignature());
        formDataParts.put("policy", preSignedPost.getPolicy());
        return formDataParts;
    }

    protected static Set<String[]> getCommonConditions() {
        Set<String[]> conditions = new HashSet<>();
        conditions.add(new String[] {"eq", "$key", "test.txt"});
        conditions.add(new String[] {"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"});
        conditions.add(
                new String[] {"eq", "$x-amz-date", TestUtils.getAmzDateFormatter().format(DATE)});
        conditions.add(new String[] {"eq", "$bucket", BUCKET});
        return conditions;
    }

    protected boolean uploadToAws(Map<String, String> formDataParts, String url) {
        Request request = createRequest(formDataParts, url);
        return postFileIntoS3(request);
    }

    private Request createRequest(Map<String, String> formDataParts, String url) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        formDataParts.forEach(builder::addFormDataPart);
        // file has to be the last parameter according to aws
        builder.addFormDataPart(
                "file",
                "test.txt",
                RequestBody.create("this is a test".getBytes(), MediaType.parse("text/plain")));
        MultipartBody multipartBody = builder.build();
        return new Request.Builder().url(url).post(multipartBody).build();
    }

    protected static String getCredential() {
        return String.format(
                "%s/%s/%s/s3/aws4_request",
                System.getenv("AWS_ACCESS_KEY_ID"),
                YYYYMMDD_DATE_FORMATTER.format(DATE),
                System.getenv("AWS_REGION"));
    }

    protected String getUrl() {
        return String.format(
                "https://%s.s3.%s.amazonaws.com",
                System.getenv("AWS_BUCKET"), System.getenv("AWS_REGION"));
    }

    protected static FreeTextPostParams getFreeTextPostParams(Set<String[]> conditions) {
        return new FreeTextPostParams(REGION, EXPIRATION_DATE, DATE, conditions);
    }
}
