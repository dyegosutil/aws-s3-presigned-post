package domain;

import mendes.sutil.dyego.awspresignedpost.AmzDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

class AmzDateTest {

    @Test
    void formatForPolicyTest() {
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
    void formatForCredentialsTest() {
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
    void formatForSigningKeyTest() {
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