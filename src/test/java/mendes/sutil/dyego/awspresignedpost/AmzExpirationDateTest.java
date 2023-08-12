package mendes.sutil.dyego.awspresignedpost;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AmzExpirationDateTest {

    @Test
    void isExpiredTest() {
        // Arrange
        ZonedDateTime dateTimeInThePast = Instant.now(Clock.systemUTC())
                .minus(1, ChronoUnit.SECONDS)
                .atZone(ZoneOffset.UTC);
        AmzExpirationDate amzExpirationDate = new AmzExpirationDate(dateTimeInThePast);

        // Act & Assert
        assertThat(amzExpirationDate.isExpired()).isTrue();
    }

}