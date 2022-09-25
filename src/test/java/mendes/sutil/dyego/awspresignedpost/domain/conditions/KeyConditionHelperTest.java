package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import org.junit.jupiter.api.Test;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.helper.KeyConditionHelper.withKey;
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