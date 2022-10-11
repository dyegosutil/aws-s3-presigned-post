package mendes.sutil.dyego.awspresignedpost.domain;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents the expiration date time used in the policy to limit until when the pre-signed post can be used.
 */
public class AmzExpirationDate {
    
    private static final DateTimeFormatter ISO8601_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private final ZonedDateTime expirationDate;

    public AmzExpirationDate(ZonedDateTime expirationDate) {
        // TODO Possible enforce UTC
        this.expirationDate = expirationDate;
    }

    public String formatForPolicy() {
        return ISO8601_FORMATTER.format(expirationDate);
    }
}