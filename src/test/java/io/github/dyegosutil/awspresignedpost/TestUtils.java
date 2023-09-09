package io.github.dyegosutil.awspresignedpost;

import static io.github.dyegosutil.awspresignedpost.conditions.KeyConditionUtil.withAnyKey;

import io.github.dyegosutil.awspresignedpost.postparams.PostParams;

import software.amazon.awssdk.regions.Region;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TestUtils {

    public static final ZonedDateTime EXPIRATION_DATE =
            Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.MINUTES).atZone(ZoneOffset.UTC);

    public static PostParams createPostParamsWithKeyStartingWith() {
        return PostParams.builder(Region.AP_EAST_1, EXPIRATION_DATE, "myBucket", withAnyKey())
                .build();
    }

    public static DateTimeFormatter getAmzDateFormatter() {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC);
    }

    public static DateTimeFormatter getYyyyMmDdDateFormatter() {
        return DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH).withZone(ZoneOffset.UTC);
    }
}
