package io.github.dyegosutil.awspresignedpost.conditions;

import io.github.dyegosutil.awspresignedpost.conditions.key.ExactKeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyStartingWithCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyConditionUtilTest {

    @Test
    public void shouldCreateExactKeyCondition() {
        // Arrange
        String keyValue = "my_file_name.txt";

        // Act
        KeyCondition keyCondition = KeyConditionUtil.withKey(keyValue);

        // Assert
        assertThat(keyCondition).isInstanceOf(ExactKeyCondition.class);
        assertThat(keyCondition.getValue()).isEqualTo(keyValue);
    }

    @Test
    public void shouldCreateStartWithKeyCondition() {
        // Arrange
        String value = "/user/leo";

        // Act
        KeyCondition startWithCondition = KeyConditionUtil.withKeyStartingWith(value);

        // Assert
        assertThat(startWithCondition).isInstanceOf(KeyStartingWithCondition.class);
        assertThat(startWithCondition.getValue()).isEqualTo(value);
    }

    @Test
    public void shouldCreateStartWithKeyConditionWithAnyKey() {
        // Act
        KeyCondition startWithCondition = KeyConditionUtil.withAnyKey();

        // Assert
        assertThat(startWithCondition).isInstanceOf(KeyStartingWithCondition.class);
        assertThat(startWithCondition.getValue()).isEqualTo("");
    }
}
