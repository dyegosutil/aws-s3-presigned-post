package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.*;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class MandatoryPostParamsIntegrationTests extends IntegrationTests {

    /**
     * Generates the pre-signed post using the minimum mandatory params and performs the upload to S3 using a http client.
     *
     * @param testDescription
     * @param region
     * @param expirationDate
     * @param bucket
     * @param keyCondition
     * @param formDataParts
     * @param expectedResult
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesMandatoryParams")
    void testWithMandatoryParams(
            String testDescription,
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyCondition keyCondition,
            Map<String, String> formDataParts,
            Boolean expectedResult
    ) {
        PostParams postParams = PostParams // TODO perhaps this can come from the Arguments in the sourceTest method
                .builder(
                        region,
                        expirationDate,
                        bucket,
                        keyCondition
                )
                .build();

        createPreSignedPostAndUpload(postParams, formDataParts, expectedResult);
    }

    private static Stream<Arguments> getTestCasesMandatoryParams() {
        final Region incorrectRegion = Region.of(System.getenv("AWS_WRONG_REGION"));

        return Stream.of(
                // key
                of("Should succeed while uploading file to S3 using the exact key specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        true
                ),
                // key
                of("Should fail while uploading file to S3 using a different key then specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "different_key.txt"),
                        false
                ),
                // bucket
                of("Should fail while uploading file to S3 using a different bucket then the one configured in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        "wrongbucket",
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // region
                of("Should fail while uploading file to S3 using a different region then the one configured in the policy",
                        incorrectRegion,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // expiration date
                of("Should fail while uploading file to S3 when the expiration date has passed",
                        REGION,
                        getInvalidExpirationDate(),
                        BUCKET,
                        withKey("test.txt"),
                        createFormDataParts("key", "test.txt"),
                        false
                ),
                // key starts-with
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "user/leo/file.txt"),
                        true
                ),
                // key starts-with
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withKeyStartingWith("user/leo/"),
                        createFormDataParts("key", "file.txt"),
                        false
                ),
                // key starts-with anything - used also when the file name provided by the user should be used
                of("Should succeed while uploading file to S3 when key correctly starts-with the value specified in the policy",
                        REGION,
                        EXPIRATION_DATE,
                        BUCKET,
                        withAnyKey(),
                        createFormDataParts("key", "file.txt"),
                        true
                )
        );
    }

    private static ZonedDateTime getInvalidExpirationDate() {
        return Instant.now(Clock.systemUTC()) // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
                .minus(1, ChronoUnit.MILLIS)
                .atZone(ZoneOffset.UTC);
    }

}
