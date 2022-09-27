package mendes.sutil.dyego.awspresignedpost.domain.conditions.key;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyStartsWithConditionTest {

    @Test
    void pira() {
        // Arrange
        final String value = "/user/leo";

        // Act
        KeyStartsWithCondition keyStartsWithCondition = new KeyStartsWithCondition(value);

        // Assert
        assertThat(keyStartsWithCondition.getValue()).isEqualTo(value);
    }

}