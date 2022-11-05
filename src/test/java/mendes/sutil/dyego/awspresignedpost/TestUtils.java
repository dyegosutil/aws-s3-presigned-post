package mendes.sutil.dyego.awspresignedpost;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TestUtils {

    // TODO check if clock should be a parameter, check documentation to see how expiration time should be received, check what would happen if different zoneids are used for expiration aand for date in the policy
    public static final ZonedDateTime EXPIRATION_DATE = Instant.now(Clock.systemUTC())
            .plus(10, ChronoUnit.MINUTES)
            .atZone(ZoneOffset.UTC);
}
