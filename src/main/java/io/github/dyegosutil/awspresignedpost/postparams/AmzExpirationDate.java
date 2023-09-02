package io.github.dyegosutil.awspresignedpost.postparams;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents the expiration date time used in the policy to limit until when the pre-signed post
 * can be used.
 */
public class AmzExpirationDate {

    private static final DateTimeFormatter ISO8601_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                    .withZone(ZoneOffset.UTC);

    private final ZonedDateTime expirationDate;

    public AmzExpirationDate(ZonedDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String formatForPolicy() {
        return ISO8601_FORMATTER.format(expirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmzExpirationDate that = (AmzExpirationDate) o;
        return Objects.equals(expirationDate, that.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expirationDate);
    }

    public boolean isExpired() {
        ZonedDateTime dateTimeNow = Instant.now(Clock.systemUTC()).atZone(ZoneOffset.UTC);
        return expirationDate.isBefore(dateTimeNow);
    }
}
