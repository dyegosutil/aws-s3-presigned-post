package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExactKeyConditionTest {

    @Test
    public void shouldSetValueCorrectly() {
        // Arrange
        String expectedExactKeyValue = "my_key_name.txt";

        // Act
        ExactKeyCondition exactKeyCondition = new ExactKeyCondition(expectedExactKeyValue);

        // Assert
        assertThat(exactKeyCondition.getValue()).isEqualTo(expectedExactKeyValue);
    }
}