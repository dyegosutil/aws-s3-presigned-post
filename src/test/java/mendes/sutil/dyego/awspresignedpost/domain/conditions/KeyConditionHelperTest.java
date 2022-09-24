package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import org.junit.jupiter.api.Test;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.KeyConditionHelper.withKey;
import static org.assertj.core.api.Assertions.assertThat;

class KeyConditionHelperTest {

    @Test
    public void shouldCreateExactKeyConditionWithKeyValue() {
        // Arrange
        String keyValue = "my_file_name.txt";

        // Act
        KeyCondition keyCondition = withKey(keyValue);

        // Assert
        assertThat(keyCondition).isInstanceOf(ExactKeyCondition.class);
        assertThat(keyCondition.getValue()).isEqualTo(keyValue);
    }
}