package mendes.sutil.dyego.awspresignedpost.conditions.key;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
