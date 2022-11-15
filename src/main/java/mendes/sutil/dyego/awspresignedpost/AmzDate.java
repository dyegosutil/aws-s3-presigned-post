package mendes.sutil.dyego.awspresignedpost;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Used in the policy, signature and subsequently in the request
 */
public class AmzDate {
    private static final DateTimeFormatter AMZ_DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
                    .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter YYYYMMDD_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

     private final ZonedDateTime date;

    public AmzDate() { // TODO simplify this? I think of() is in order
        this.date = ZonedDateTime.now(Clock.systemUTC());
    }

    public AmzDate(ZonedDateTime date) {
        this.date = date;
    }

    /**
     * Used for testing purpose only.
     */
    public AmzDate(Clock clock) {
        this.date = ZonedDateTime.now(clock);
    }

    /**
     * @return The date formatted in ISO8601 required by AWS
     */
    public String formatForPolicy() {
        return AMZ_DATE_FORMATTER.format(date);
    }

    /**
     * @return The date in the format yyyyMMdd
     */
    public String formatForSigningKey() {
        return YYYYMMDD_DATE_FORMATTER.format(date);
    }

    /**
     * @return The date in the format yyyyMMdd
     */
    public String formatForCredentials() {
        return YYYYMMDD_DATE_FORMATTER.format(date);
    }
}
