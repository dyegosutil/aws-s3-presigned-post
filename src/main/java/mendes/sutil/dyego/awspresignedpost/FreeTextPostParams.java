package mendes.sutil.dyego.awspresignedpost;

import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * TODO Does not return all necessary params to work
 */
public final class FreeTextPostParams extends PostParmsParent{

    private final Set<String[]> conditions;
    private final ZonedDateTime date;
    public FreeTextPostParams(
            Region region,
            ZonedDateTime expirationDate,
            ZonedDateTime date,
            Set<String[]> conditions
    ){
        super(region, expirationDate);
        if (Stream.of(region, amzExpirationDate, date, conditions).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("There should be no null arguments passed to "+getClass().getName()+" constructor"); // ADD test
        }
        this.conditions = conditions;
        this.date = date;
    }

    public Set<String[]> getConditions() {
        return conditions;
    }

    public ZonedDateTime getDate() {
        return date;
    }
}

