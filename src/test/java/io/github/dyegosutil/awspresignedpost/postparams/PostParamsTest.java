package io.github.dyegosutil.awspresignedpost.postparams;

import io.github.dyegosutil.awspresignedpost.TestUtils;
import io.github.dyegosutil.awspresignedpost.conditions.*;
import io.github.dyegosutil.awspresignedpost.conditions.key.ExactKeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyStartingWithCondition;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.dyegosutil.awspresignedpost.conditions.ConditionField.*;
import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.EQ;
import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.STARTS_WITH;
import static io.github.dyegosutil.awspresignedpost.postparams.PostParams.Builder.CannedAcl.PRIVATE;
import static io.github.dyegosutil.awspresignedpost.postparams.PostParams.Builder.EncryptionAlgorithm.AWS_KMS;
import static io.github.dyegosutil.awspresignedpost.postparams.PostParams.Builder.StorageClass.STANDARD;
import static io.github.dyegosutil.awspresignedpost.postparams.PostParams.Builder.SuccessActionStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

class PostParamsTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldTestIfConditionWasAddedTestCases")
    void shouldTestIfConditionsWereAdded(
            String testName,
            Supplier<PostParams.Builder> builderSupplier,
            ConditionField expectedConditionField,
            MatchCondition.Operator expectedOperator) {

        // Arrange & Act
        Map<ConditionField, Condition> conditions = builderSupplier.get().build().getConditions();

        // Assert
        assertThat(conditions)
                .containsValue(
                        new MatchCondition(expectedConditionField, expectedOperator, "test"));
    }

    @Test
    void shouldTestIfConditionsWereAddedForConditionsServerSideEncryptionCustomer() {
        // Arrange
        PostParams postParams =
                createBuilder()
                        .withServerSideEncryptionCustomerAlgorithmAES256()
                        .withServerSideEncryptionCustomerKey("test")
                        .withServerSideEncryptionCustomerKeyMD5("test")
                        .build();

        // Act
        Map<ConditionField, Condition> conditions = postParams.getConditions();

        // Assert
        Assertions.assertThat(conditions)
                .containsValues(
                        new MatchCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, EQ, "AES256"),
                        new MatchCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, EQ, "test"),
                        new MatchCondition(SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5, EQ, "test"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldThrowAnErrorIfRequiredConditionsWereNotAdded")
    void shouldThrowAnErrorIfRequiredConditionsWereNotAdded(
            String testName,
            ThrowableAssert.ThrowingCallable prohibitedDoubleConditionCall,
            String exceptionMessage) {
        assertThatThrownBy(prohibitedDoubleConditionCall)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldTestIfChecksumConditionWasAddedTestCases")
    void shouldTestIfChecksumConditionsWereAdded(
            String testName,
            Supplier<PostParams.Builder> builderSupplier,
            ConditionField expectedConditionField) {

        // Arrange & act
        Map<ConditionField, Condition> conditions = builderSupplier.get().build().getConditions();

        // Assert
        Assertions.assertThat(conditions)
                .containsValue(new ChecksumCondition(expectedConditionField, "test"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldTestIfMetaConditionWasAddedTestCases")
    void shouldTestIfMetaConditionsWereAdded(
            String testName,
            Supplier<PostParams.Builder> builderSupplier,
            MatchCondition.Operator expectedOperator) {

        // Arrange & act
        Map<ConditionField, Condition> conditions = builderSupplier.get().build().getConditions();

        // Assert
        assertThat(conditions).containsValue(new MetaCondition(expectedOperator, "test", "test"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldAssertSingleMutuallyExclusiveConditionsTest")
    void shouldAssertSingleMutuallyExclusiveConditions(
            String testName,
            ThrowableAssert.ThrowingCallable prohibitedDoubleConditionCall,
            String exceptionMessage) {
        assertThatThrownBy(prohibitedDoubleConditionCall)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    @Test
    void createBuilderTest() {
        // Act
        PostParams.Builder builder =
                PostParams.builder(
                        Region.AP_EAST_1,
                        TestUtils.EXPIRATION_DATE,
                        "testBucket",
                        KeyConditionUtil.withAnyKey());

        // Assert
        assertThat(builder).isNotNull();
    }

    @Test
    void createPostParamTest() {
        AmzExpirationDate amzExpirationDate = new AmzExpirationDate(TestUtils.EXPIRATION_DATE);
        // Act
        PostParams postParams =
                PostParams.builder(
                                Region.AP_EAST_1,
                                TestUtils.EXPIRATION_DATE,
                                "testBucket",
                                KeyConditionUtil.withAnyKey())
                        .build();

        // Assert
        assertThat(postParams.getRegion()).isEqualTo(Region.AP_EAST_1);
        assertThat(postParams.getBucket()).isEqualTo("testBucket");
        assertThat(postParams.getAmzExpirationDate()).isEqualTo(amzExpirationDate);
    }

    @ParameterizedTest(name = "Should assert that {0} is not null")
    @MethodSource("createBuilderWithNullArgKeyStartingWithConditionTestCases")
    void createBuilderNullArgKeyStartingWithConditionTest(
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            KeyStartingWithCondition keyCondition,
            String errorMessage) {
        assertThatThrownBy(() -> PostParams.builder(region, expirationDate, bucket, keyCondition))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(errorMessage);
    }

    @ParameterizedTest(name = "Should assert that {0} is not null")
    @MethodSource("createBuilderWithNullArgExactKeyConditionTestCases")
    void createBuilderNullArgExactKeyConditionTest(
            Region region,
            ZonedDateTime expirationDate,
            String bucket,
            ExactKeyCondition keyCondition,
            String errorMessage) {
        assertThatThrownBy(() -> PostParams.builder(region, expirationDate, bucket, keyCondition))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(errorMessage);
    }

    @Test
    void contentLengthRangeConditionTest() {
        // Act
        PostParams postParams =
                PostParams.builder(
                                Region.AP_EAST_1,
                                TestUtils.EXPIRATION_DATE,
                                "testBucket",
                                KeyConditionUtil.withAnyKey())
                        .withContentLengthRange(100, 200)
                        .build();

        // Assert
        assertThat(postParams.getConditions())
                .containsValue(new ContentLengthRangeCondition(100, 200));
    }

    @Test
    void shouldThrowExceptionWhenBuilderWithKeyStartingWithExpirationDateIsExpired() {
        ZonedDateTime expirationDateInThePast = getExpirationDateInThePast();
        String formattedDateTimeInThePast =
                new AmzExpirationDate(expirationDateInThePast).formatForPolicy();

        assertThatThrownBy(
                        () ->
                                PostParams.builder(
                                        Region.AP_EAST_1,
                                        expirationDateInThePast,
                                        "testBucket",
                                        KeyConditionUtil.withAnyKey()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "The condition expiration date "
                                + formattedDateTimeInThePast
                                + " already expired");
    }

    @Test
    void shouldThrowExceptionWhenBuilderWithKeyExpirationDateIsExpired() {
        ZonedDateTime expirationDateInThePast = getExpirationDateInThePast();
        String formattedDateTimeInThePast =
                new AmzExpirationDate(expirationDateInThePast).formatForPolicy();

        assertThatThrownBy(
                        () ->
                                PostParams.builder(
                                        Region.AP_EAST_1,
                                        expirationDateInThePast,
                                        "testBucket",
                                        KeyConditionUtil.withKey("myFile.txt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "The condition expiration date "
                                + formattedDateTimeInThePast
                                + " already expired");
    }

    private ZonedDateTime getExpirationDateInThePast() {
        return Instant.now(Clock.systemUTC()).minus(1, ChronoUnit.SECONDS).atZone(ZoneOffset.UTC);
    }

    private static Stream<Arguments> createBuilderWithNullArgExactKeyConditionTestCases() {
        return Stream.of(
                of(
                        null,
                        ZonedDateTime.now(),
                        "testBucket",
                        KeyConditionUtil.withKey("test"),
                        "Argument region must not be null"),
                of(
                        Region.AP_EAST_1,
                        null,
                        "testBucket",
                        KeyConditionUtil.withKey("test"),
                        "Argument expirationDate must not be null"),
                of(
                        Region.AP_EAST_1,
                        ZonedDateTime.now(),
                        null,
                        KeyConditionUtil.withKey("test"),
                        "Argument bucket must not be null"),
                of(
                        Region.AP_EAST_1,
                        ZonedDateTime.now(),
                        "testBucket",
                        null,
                        "Argument keyCondition must not be null"));
    }

    private static Stream<Arguments> createBuilderWithNullArgKeyStartingWithConditionTestCases() {
        return Stream.of(
                of(
                        null,
                        ZonedDateTime.now(),
                        "testBucket",
                        KeyConditionUtil.withAnyKey(),
                        "Argument region must not be null"),
                of(
                        Region.AP_EAST_1,
                        null,
                        "testBucket",
                        KeyConditionUtil.withAnyKey(),
                        "Argument expirationDate must not be null"),
                of(
                        Region.AP_EAST_1,
                        ZonedDateTime.now(),
                        null,
                        KeyConditionUtil.withAnyKey(),
                        "Argument bucket must not be null"),
                of(
                        Region.AP_EAST_1,
                        ZonedDateTime.now(),
                        "testBucket",
                        null,
                        "Argument keyCondition must not be null"));
    }

    private static Stream<Arguments> shouldTestIfChecksumConditionWasAddedTestCases() {
        return Stream.of(
                of(
                        "Should assert that condition withChecksumSha256 was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withChecksumSha256("test"),
                        CHECKSUM_SHA256),
                of(
                        "Should assert that condition withChecksumSha1 was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withChecksumSha1("test"),
                        CHECKSUM_SHA1),
                of(
                        "Should assert that condition withChecksumCrc32 was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withChecksumCrc32("test"),
                        CHECKSUM_CRC32),
                of(
                        "Should assert that condition withChecksumCrc32c was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withChecksumCrc32c("test"),
                        CHECKSUM_CRC32C));
    }

    private static Stream<Arguments> shouldTestIfConditionWasAddedTestCases() {
        return Stream.of(
                of(
                        "Should assert that condition withCacheControlStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withCacheControlStartingWith("test"),
                        CACHE_CONTROL,
                        STARTS_WITH),
                of(
                        "Should assert that condition withCacheControl was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withCacheControl("test"),
                        CACHE_CONTROL,
                        EQ),
                of(
                        "Should assert that condition withAnyCacheControl was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withAnyCacheControl(),
                        CACHE_CONTROL,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentTypeStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentTypeStartingWith("test"),
                        CONTENT_TYPE,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentType was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentType("test"),
                        CONTENT_TYPE,
                        EQ),
                of(
                        "Should assert that condition withAnyContentType was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withAnyContentType(),
                        CONTENT_TYPE,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentDispositionStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentDispositionStartingWith("test"),
                        CONTENT_DISPOSITION,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentDisposition was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentDisposition("test"),
                        CONTENT_DISPOSITION,
                        EQ),
                of(
                        "Should assert that condition withAnyContentDisposition was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withAnyContentDisposition(),
                        CONTENT_DISPOSITION,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentEncodingStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentEncodingStartingWith("test"),
                        CONTENT_ENCODING,
                        STARTS_WITH),
                of(
                        "Should assert that condition withContentEncoding was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withContentEncoding("test"),
                        CONTENT_ENCODING,
                        EQ),
                of(
                        "Should assert that condition withAnyContentEncoding was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withAnyContentEncoding(),
                        CONTENT_ENCODING,
                        EQ),
                of(
                        "Should assert that condition withExpiresStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withExpiresStartingWith("test"),
                        EXPIRES,
                        STARTS_WITH),
                of(
                        "Should assert that condition withExpires was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withExpires("test"),
                        EXPIRES,
                        EQ),
                of(
                        "Should assert that condition withAnyExpires was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withAnyExpires(),
                        EXPIRES,
                        STARTS_WITH),
                of(
                        "Should assert that condition withSuccessActionRedirectStartingWith was"
                                + " added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withSuccessActionRedirectStartingWith("test"),
                        SUCCESS_ACTION_REDIRECT,
                        STARTS_WITH),
                of(
                        "Should assert that condition withSuccessActionRedirect was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withSuccessActionRedirect("test"),
                        SUCCESS_ACTION_REDIRECT,
                        EQ),
                of(
                        "Should assert that condition withAnySuccessActionRedirect was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withAnySuccessActionRedirect(),
                        SUCCESS_ACTION_REDIRECT,
                        STARTS_WITH),
                of(
                        "Should assert that condition withSuccessActionStatus was added",
                        (Supplier<PostParams.Builder>)
                                () ->
                                        createBuilder()
                                                .withSuccessActionStatus(SuccessActionStatus.OK),
                        SUCCESS_ACTION_STATUS,
                        EQ),
                of(
                        "Should assert that condition withAclStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withAclStartingWith("test"),
                        ACL,
                        STARTS_WITH),
                of(
                        "Should assert that condition withAcl was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withAcl(PRIVATE),
                        ACL,
                        EQ),
                of(
                        "Should assert that condition withAnyAcl was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withAnyAcl(),
                        ACL,
                        STARTS_WITH),
                of(
                        "Should assert that condition withTagging was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder().withTagging("test"),
                        TAGGING,
                        EQ),
                of(
                        "Should assert that condition withTag was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withTag("key", "value"),
                        TAGGING,
                        EQ),
                of(
                        "Should assert that condition withStorageClass was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withStorageClass(STANDARD),
                        STORAGE_CLASS,
                        EQ),
                of(
                        "Should assert that condition withWebsiteRedirectLocation was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withWebsiteRedirectLocation("test"),
                        WEBSITE_REDIRECT_LOCATION,
                        EQ),
                of(
                        "Should assert that condition withServerSideEncryption was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withServerSideEncryption(AWS_KMS),
                        SERVER_SIDE_ENCRYPTION,
                        EQ),
                of(
                        "Should assert that condition withServerSideEncryptionAwsKmsKeyId was"
                                + " added",
                        (Supplier<PostParams.Builder>)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryption(AWS_KMS)
                                                .withServerSideEncryptionAwsKmsKeyId("test"),
                        SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID,
                        EQ),
                of(
                        "Should assert that condition withServerSideEncryptionContext was added",
                        (Supplier<PostParams.Builder>)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryption(AWS_KMS)
                                                .withServerSideEncryptionContext("test"),
                        SERVER_SIDE_ENCRYPTION_CONTEXT,
                        EQ),
                of(
                        "Should assert that condition withServerSideEncryptionBucketKeyEnabled was"
                                + " added",
                        (Supplier<PostParams.Builder>)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryption(AWS_KMS)
                                                .withServerSideEncryptionBucketKeyEnabled(),
                        SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED,
                        EQ));
    }

    private static Stream<Arguments> shouldTestIfMetaConditionWasAddedTestCases() {
        return Stream.of(
                of(
                        "Should assert that condition withMeta was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withMeta("test", "test"),
                        EQ),
                of(
                        "Should assert that condition withMetaStartingWith was added",
                        (Supplier<PostParams.Builder>)
                                () -> createBuilder().withMetaStartingWith("test", "test"),
                        STARTS_WITH));
    }

    private static Stream<Arguments> shouldThrowAnErrorIfRequiredConditionsWereNotAdded() {
        return Stream.of(
                of(
                        "Should assert that withServerSideEncryptionCustomerAlgorithmAES256 is"
                                + " called with required conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionCustomerAlgorithmAES256()
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM requires the"
                                + " condition(s) [SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY,"
                                + " SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5] to be present"),
                of(
                        "Should assert that withServerSideEncryptionCustomerKey is called with"
                                + " required conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionCustomerKey("test")
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY requires the"
                                + " condition(s) [SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM,"
                                + " SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5] to be present"),
                of(
                        "Should assert that withServerSideEncryptionCustomerKeyMD5 is called with"
                                + " required conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionCustomerKeyMD5("test")
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5 requires the"
                                + " condition(s) [SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM,"
                                + " SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY] to be present"),
                of(
                        "Should assert that withServerSideEncryptionAwsKmsKeyId is called with"
                                + " required conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionAwsKmsKeyId("test")
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_AWS_KMS_KEY_ID requires the"
                                + " condition(s) [SERVER_SIDE_ENCRYPTION] to be present"),
                of(
                        "Should assert that withServerSideEncryptionContext is called with required"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionContext("test")
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_CONTEXT requires the condition(s)"
                                + " [SERVER_SIDE_ENCRYPTION] to be present"),
                of(
                        "Should assert that withServerSideEncryptionBucketKeyEnabled is called with"
                                + " required conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withServerSideEncryptionBucketKeyEnabled()
                                                .build(),
                        "The condition SERVER_SIDE_ENCRYPTION_BUCKET_KEY_ENABLED requires the"
                                + " condition(s) [SERVER_SIDE_ENCRYPTION] to be present"));
    }

    private static Stream<Arguments> shouldAssertSingleMutuallyExclusiveConditionsTest() {
        return Stream.of(
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ"
                                + " CACHE_CONTROL conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withCacheControlStartingWith("test")
                                                .withCacheControl("test"),
                        getExceptionMessage(CACHE_CONTROL)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH"
                                + " CACHE_CONTROL conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withCacheControl("test")
                                                .withCacheControlStartingWith("test"),
                        getExceptionMessage(CACHE_CONTROL)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ CONTENT_TYPE"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentTypeStartingWith("test")
                                                .withContentType("test"),
                        getExceptionMessage(CONTENT_TYPE)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH CONTENT_TYPE"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentType("test")
                                                .withContentTypeStartingWith("test"),
                        getExceptionMessage(CONTENT_TYPE)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ"
                                + " CONTENT_DISPOSITION conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentDispositionStartingWith("test")
                                                .withContentDisposition("test"),
                        getExceptionMessage(CONTENT_DISPOSITION)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH"
                                + " CONTENT_DISPOSITION conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentDisposition("test")
                                                .withContentDispositionStartingWith("test"),
                        getExceptionMessage(CONTENT_DISPOSITION)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ"
                                + " CONTENT_ENCODING conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentEncodingStartingWith("test")
                                                .withContentEncoding("test"),
                        getExceptionMessage(CONTENT_ENCODING)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH"
                                + " CONTENT_ENCODING conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withContentEncoding("test")
                                                .withContentEncodingStartingWith("test"),
                        getExceptionMessage(CONTENT_ENCODING)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ EXPIRES"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withExpiresStartingWith("test")
                                                .withExpires("test"),
                        getExceptionMessage(EXPIRES)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH EXPIRES"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withExpires("test")
                                                .withExpiresStartingWith("test"),
                        getExceptionMessage(EXPIRES)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ"
                                + " SUCCESS_ACTION_REDIRECT conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withSuccessActionRedirectStartingWith("test")
                                                .withSuccessActionRedirect("test"),
                        getExceptionMessage(SUCCESS_ACTION_REDIRECT)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH"
                                + " SUCCESS_ACTION_REDIRECT conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withSuccessActionRedirect("test")
                                                .withSuccessActionRedirectStartingWith("test"),
                        getExceptionMessage(SUCCESS_ACTION_REDIRECT)),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ ACL"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () -> createBuilder().withAclStartingWith("test").withAcl(PRIVATE),
                        getExceptionMessage(ACL)),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH ACL"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () -> createBuilder().withAcl(PRIVATE).withAclStartingWith("test"),
                        getExceptionMessage(ACL)),
                of(
                        "Should assert that there is no conflicting withTag and withTagging TAGGING"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () -> createBuilder().withTagging("test").withTag("key", "value"),
                        getTaggingExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withTagging and withTag TAGGING"
                                + " conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () -> createBuilder().withTag("key", "value").withTagging("test"),
                        getTaggingExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32 and"
                                + " withChecksumCrc32c checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32("test")
                                                .withChecksumCrc32c("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32 and"
                                + " withChecksumSha1 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32("test")
                                                .withChecksumSha1("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32 and"
                                + " withChecksumSha256 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32("test")
                                                .withChecksumSha256("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32c and"
                                + " withChecksumSha1 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32c("test")
                                                .withChecksumSha1("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32c and"
                                + " withChecksumSha256 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32c("test")
                                                .withChecksumSha256("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumSha1 and"
                                + " withChecksumSha256 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumSha1("test")
                                                .withChecksumSha256("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumSha1 and"
                                + " withChecksumSha1 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumSha1("test")
                                                .withChecksumSha1("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumSha256 and"
                                + " withChecksumSha256 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumSha256("test")
                                                .withChecksumSha256("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32c and"
                                + " withChecksumCrc32c checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32c("test")
                                                .withChecksumCrc32c("test"),
                        getChecksumExceptionMessage()),
                of(
                        "Should assert that there is no conflicting withChecksumCrc32 and"
                                + " withChecksumCrc32 checksum conditions",
                        (ThrowableAssert.ThrowingCallable)
                                () ->
                                        createBuilder()
                                                .withChecksumCrc32("test")
                                                .withChecksumCrc32("test"),
                        getChecksumExceptionMessage()));
    }

    private static String getTaggingExceptionMessage() {
        return "Either the method withTag() or withTagging() can be used for adding tagging, not"
                + " both";
    }

    private static String getChecksumExceptionMessage() {
        return "Only one checksum condition CRC32, CRC32C, SHA1 or SHA256 can be added at the same"
                + " time";
    }

    private static String getExceptionMessage(ConditionField conditionField) {
        return String.format("Only one %s condition can be used", conditionField.name());
    }

    private static PostParams.Builder createBuilder() {
        return PostParams.builder(
                Region.AP_EAST_1,
                TestUtils.EXPIRATION_DATE,
                "testBucket",
                KeyConditionUtil.withAnyKey());
    }
}
