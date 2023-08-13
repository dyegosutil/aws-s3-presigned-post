package mendes.sutil.dyego.awspresignedpost.postparams;

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

  /**
   * A versatile and flexible option to build the post params to be used to generate a pre signed
   * post. Only minimal validations are executed giving total freedom to choose the conditions. Bear
   * in mind that this adds complexity, it will be more error-prone if all the necessary S3 AWS
   * rules are not followed.
   *
   * @param expirationDate Indicates until when the pre signed post must be valid
   * @param date Date to be used in the policy and in the credential field. Not to be confused with
   *     expirationDate
   * @param conditions Conditions such as content length range, checksum sha256, success action
   *     redirect, etc
   */
  public FreeTextPostParams(
      Region region, ZonedDateTime expirationDate, ZonedDateTime date, Set<String[]> conditions) {
    if (Stream.of(region, expirationDate, date, conditions).anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException(
          "There should be no null arguments passed to "
              + getClass().getSimpleName()
              + " constructor");
    }
    if (conditions.isEmpty()) {
      throw new IllegalArgumentException(
          "The conditions for " + getClass().getSimpleName() + " should not be empty");
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
