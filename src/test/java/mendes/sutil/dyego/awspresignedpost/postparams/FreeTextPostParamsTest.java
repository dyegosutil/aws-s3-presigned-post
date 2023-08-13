package mendes.sutil.dyego.awspresignedpost.postparams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

class FreeTextPostParamsTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void shouldThrowExceptionWhenUsingNullArgumentsCreatingFreeTextPostParams(
            String testCaseName,
            Supplier<FreeTextPostParams> supplier
    ) {
        assertThatThrownBy(supplier::get)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There should be no null arguments passed to FreeTextPostParams constructor");
    }

    @Test
    void shouldThrowExceptionWhenUsingEmptySetOfConditionsCreatingFreeTextPostParams() {

        // when
        assertThatThrownBy(
                () -> new FreeTextPostParams(Region.AP_EAST_1, ZonedDateTime.now(), ZonedDateTime.now(), new HashSet<>())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The conditions for FreeTextPostParams should not be empty");
    }

    public static Stream<Arguments> getTestCases() {
        return Stream.of(
                of(
                        "Should throw a IllegalArgumentException when region is null",
                        (Supplier<FreeTextPostParams>) () -> new FreeTextPostParams(null, ZonedDateTime.now(), ZonedDateTime.now(), new HashSet<>())
                ),
                of(
                        "Should throw an IllegalArgumentException when expirationDate is null",
                        (Supplier<FreeTextPostParams>) () -> new FreeTextPostParams(Region.AP_EAST_1, null, ZonedDateTime.now(), new HashSet<>())
                ),
                of(
                        "Should throw an IllegalArgumentException when date is null",
                        (Supplier<FreeTextPostParams>) () -> new FreeTextPostParams(Region.AP_EAST_1, ZonedDateTime.now(), null, new HashSet<>())
                ),
                of(
                        "Should throw an IllegalArgumentException when conditions is null",
                        (Supplier<FreeTextPostParams>) () -> new FreeTextPostParams(Region.AP_EAST_1, ZonedDateTime.now(), ZonedDateTime.now(), null)
                )
        );
    }
}