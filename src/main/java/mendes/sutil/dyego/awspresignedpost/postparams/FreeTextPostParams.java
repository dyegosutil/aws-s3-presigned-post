package mendes.sutil.dyego.awspresignedpost.postparams;

import mendes.sutil.dyego.awspresignedpost.AmzExpirationDate;
import software.amazon.awssdk.regions.Region;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class FreeTextPostParams {

    private final Region region;
    private final AmzExpirationDate amzExpirationDate;
    private final ZonedDateTime date;
    private final Set<String[]> conditions;
    public FreeTextPostParams(
            Region region,
            ZonedDateTime expirationDate,
            ZonedDateTime date,
            Set<String[]> conditions
    ){
        if (Stream.of(region, expirationDate, date, conditions).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("There should be no null arguments passed to "+getClass().getSimpleName()+" constructor");
        }
        if(conditions.isEmpty()) {
            throw new IllegalArgumentException("The conditions for "+getClass().getSimpleName()+" should not be empty");
        }
        this.region = region;
        this.amzExpirationDate = new AmzExpirationDate(expirationDate);
        this.conditions = conditions;
        this.date = date;
    }

    public Region getRegion() {
        return this.region;
    }

    public AmzExpirationDate getAmzExpirationDate() {
        return this.amzExpirationDate;
    }

    public Set<String[]> getConditions() {
        return conditions;
    }

    public ZonedDateTime getDate() {
        return date;
    }
}

