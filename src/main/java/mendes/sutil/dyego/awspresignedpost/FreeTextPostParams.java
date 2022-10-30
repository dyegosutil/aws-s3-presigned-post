package mendes.sutil.dyego.awspresignedpost;

import mendes.sutil.dyego.awspresignedpost.domain.AmzExpirationDate;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class FreeTextPostParams extends PostParmsParent{  // TODO perhaps chose a better name?

    private final Set<String[]> conditions;
    private final ZonedDateTime date;
    public FreeTextPostParams(
            Region region,
            AmzExpirationDate amzExpirationDate,
            ZonedDateTime date,
            Set<String[]> conditions
    ){
        super(region, amzExpirationDate);
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

