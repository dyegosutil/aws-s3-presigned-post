package domain.conditions.key;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyStartingWithCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyStartingWithConditionTest {

    @Test
    void pira() {
        // Arrange
        final String value = "/user/leo";

        // Act
        KeyStartingWithCondition keyStartingWithCondition = new KeyStartingWithCondition(value);

        // Assert
        assertThat(keyStartingWithCondition.getValue()).isEqualTo(value);
    }

}