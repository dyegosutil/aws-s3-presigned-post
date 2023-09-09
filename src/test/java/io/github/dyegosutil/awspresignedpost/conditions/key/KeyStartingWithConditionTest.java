package io.github.dyegosutil.awspresignedpost.conditions.key;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KeyStartingWithConditionTest {

    @Test
    void shouldSetValueOfKeyStartingWithCondition() {
        // Arrange
        final String value = "/user/leo";

        // Act
        KeyStartingWithCondition keyStartingWithCondition = new KeyStartingWithCondition(value);

        // Assert
        assertThat(keyStartingWithCondition.getValue()).isEqualTo(value);
    }
}
