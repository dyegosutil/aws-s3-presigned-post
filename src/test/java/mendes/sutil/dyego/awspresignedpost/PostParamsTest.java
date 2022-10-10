package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ContentLengthRangeCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.*;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator.EQ;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator.STARTS_WITH;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.withAnyKey;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * TODO Add correspondent tests of shouldTestIfConditionsWereAdded for the mandatory params.
 */
class PostParamsTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldTestIfConditionWasAddedTestCases")
    void shouldTestIfConditionsWereAdded(
            String testName,
            Supplier<PostParams.Builder> builderSupplier,
            ConditionField expectedConditionField,
            MatchCondition.Operator expectedOperator

    ) {
        // Arrange & act
        Set<Condition> conditions = builderSupplier.get().build().getConditions();

        // Assert
        Assertions.assertThat(conditions)
                .contains(new MatchCondition(expectedConditionField, expectedOperator, "test"));
    }

    @Test
    void shouldTestIfConditionContentLengthRangeWasAdded() {
        Assertions.assertThat(
                createBuilder()
                        .withContentLengthRange(10, 20)
                        .build()
                        .getConditions()
        ).contains(new ContentLengthRangeCondition(10, 20));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shouldAssertSingleMutuallyExclusiveConditionsTest")
    void shouldAssertSingleMutuallyExclusiveConditions(
            String testName,
            ThrowableAssert.ThrowingCallable prohibitedDoubleConditionCall,
            String exceptionMessage
    ) {
        assertThatThrownBy(prohibitedDoubleConditionCall)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    private static Stream<Arguments> shouldTestIfConditionWasAddedTestCases() {
        return Stream.of(
                of(
                        "Should assert that condition withCacheControlStartingWith was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withCacheControlStartingWith("test"),
                        CACHE_CONTROL,
                        STARTS_WITH
                ),
                of(
                        "Should assert that condition withCacheControl was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withCacheControl("test"),
                        CACHE_CONTROL,
                        EQ
                ),
                of(
                        "Should assert that condition withContentTypeStartingWith was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentTypeStartingWith("test"),
                        CONTENT_TYPE,
                        STARTS_WITH
                ),
                of(
                        "Should assert that condition withContentType was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentType("test"),
                        CONTENT_TYPE,
                        EQ
                ),
                of(
                        "Should assert that condition withContentDispositionStartingWith was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentDispositionStartingWith("test"),
                        CONTENT_DISPOSITION,
                        STARTS_WITH
                ),
                of(
                        "Should assert that condition withContentDisposition was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentDisposition("test"),
                        CONTENT_DISPOSITION,
                        EQ
                ),
                of(
                        "Should assert that condition withContentEncodingStartingWith was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentEncodingStartingWith("test"),
                        CONTENT_ENCODING,
                        STARTS_WITH
                ),
                of(
                        "Should assert that condition withContentEncoding was added",
                        (Supplier<PostParams.Builder>) () -> createBuilder()
                                .withContentEncoding("test"),
                        CONTENT_ENCODING,
                        EQ
                )
        );
    }

    private static Stream<Arguments> shouldAssertSingleMutuallyExclusiveConditionsTest() {
        return Stream.of(
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ CACHE_CONTROL conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withCacheControlStartingWith("test")
                                        .withCacheControl("test"),
                        getExceptionMessage(CACHE_CONTROL)
                ),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH CACHE_CONTROL conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withCacheControl("test")
                                        .withCacheControlStartingWith("test"),
                        getExceptionMessage(CACHE_CONTROL)
                ),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ CONTENT_TYPE conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentTypeStartingWith("test")
                                        .withContentType("test"),
                        getExceptionMessage(CONTENT_TYPE)
                ),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH CONTENT_TYPE conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentType("test")
                                        .withContentTypeStartingWith("test"),
                        getExceptionMessage(CONTENT_TYPE)
                ),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ CONTENT_DISPOSITION conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentDispositionStartingWith("test")
                                        .withContentDisposition("test"),
                        getExceptionMessage(CONTENT_DISPOSITION)
                ),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH CONTENT_DISPOSITION conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentDisposition("test")
                                        .withContentDispositionStartingWith("test"),
                        getExceptionMessage(CONTENT_DISPOSITION)
                ),
                of(
                        "Should assert that there is no conflicting STARTS_WITH and EQ CONTENT_ENCODING conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentEncodingStartingWith("test")
                                        .withContentEncoding("test"),
                        getExceptionMessage(CONTENT_ENCODING)
                ),
                of(
                        "Should assert that there is no conflicting EQ and STARTS_WITH CONTENT_ENCODING conditions",
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withContentEncoding("test")
                                        .withContentEncodingStartingWith("test"),
                        getExceptionMessage(CONTENT_ENCODING)
                )
        );
    }

    private static String getExceptionMessage(ConditionField conditionField) {
        return String.format("Only one %s condition can be used", conditionField.name());
    }

    private static PostParams.Builder createBuilder() {
        return PostParams
                .builder(
                        Region.AP_EAST_1,
                        ZonedDateTime.now(),
                        "testBucket",
                        withAnyKey()
                );
    }
}