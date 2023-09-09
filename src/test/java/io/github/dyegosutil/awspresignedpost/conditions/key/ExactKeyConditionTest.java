package io.github.dyegosutil.awspresignedpost.conditions.key;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExactKeyConditionTest {

    @Test
    public void shouldSetValueOfExactKeyCondition() {
        // Arrange
        String expectedExactKeyValue = "my_key_name.txt";

        // Act
        ExactKeyCondition exactKeyCondition = new ExactKeyCondition(expectedExactKeyValue);

        // Assert
        assertThat(exactKeyCondition.getValue()).isEqualTo(expectedExactKeyValue);
    }
}
