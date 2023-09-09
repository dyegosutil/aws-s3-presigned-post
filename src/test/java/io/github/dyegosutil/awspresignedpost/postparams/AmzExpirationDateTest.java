package io.github.dyegosutil.awspresignedpost.postparams;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

class AmzExpirationDateTest {

    @Test
    void shouldTestAmzExpirationDate() {
        // Arrange
        ZonedDateTime zonedDateTime =
                Instant.now(Clock.systemUTC()).minus(1, ChronoUnit.SECONDS).atZone(ZoneOffset.UTC);
        AmzExpirationDate amzExpirationDate = new AmzExpirationDate(zonedDateTime);

        // Act & Assert
        assertThat(amzExpirationDate.isExpired()).isTrue();
        assertThat(amzExpirationDate.hashCode()).isEqualTo(Objects.hash(zonedDateTime));
    }
}
