package mendes.sutil.dyego.awspresignedpost;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class AmzDateTest {

  @Test
  void shouldGetFormatForPolicyValue() {
    // Arrange
    String instantExpected = "2022-02-22T10:15:30Z";
    Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneOffset.UTC);
    AmzDate xAmzDate = new AmzDate(clock);

    // Act
    String policyXamzDate = xAmzDate.formatForPolicy();

    // Assert
    Assertions.assertThat(policyXamzDate).isEqualTo("20220222T101530Z");
  }

  @Test
  void shouldGetFormatForCredentialsValue() {
    // Arrange
    String instantExpected = "2022-02-22T10:15:30Z";
    Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneOffset.UTC);
    AmzDate xAmzDate = new AmzDate(clock);

    // Act
    String credentialsAmzDate = xAmzDate.formatForCredentials();

    // Assert
    Assertions.assertThat(credentialsAmzDate).isEqualTo("20220222");
  }

  @Test
  void shouldGetFormatForSigningKeyValue() {
    // Arrange
    String instantExpected = "2022-02-22T10:15:30Z";
    Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneOffset.UTC);
    AmzDate xAmzDate = new AmzDate(clock);

    // Act
    String credentialsAmzDate = xAmzDate.formatForSigningKey();

    // Assert
    Assertions.assertThat(credentialsAmzDate).isEqualTo("20220222");
  }
}
