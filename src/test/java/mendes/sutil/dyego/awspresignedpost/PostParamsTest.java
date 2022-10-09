package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.CACHE_CONTROL;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.withAnyKey;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

class PostParamsTest {

    @ParameterizedTest
    @MethodSource("shouldAssertSingleMutuallyExclusiveConditionsTest")
    void shouldAssertSingleMutuallyExclusiveConditions(
            ThrowableAssert.ThrowingCallable prohibitedDoubleConditionCall,
            String exceptionMessage
    ) {
        assertThatThrownBy(prohibitedDoubleConditionCall)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    private static Stream<Arguments> shouldAssertSingleMutuallyExclusiveConditionsTest() {
        return Stream.of(
                of(
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withCacheControlStartingWith("test")
                                        .withCacheControl("test"),
                        getExceptionMessage(CACHE_CONTROL)
                ),
                of(
                        (ThrowableAssert.ThrowingCallable) () ->
                                createBuilder()
                                        .withCacheControl("test")
                                        .withCacheControlStartingWith("test"),
                        getExceptionMessage(CACHE_CONTROL)
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