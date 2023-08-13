package mendes.sutil.dyego.awspresignedpost.conditions;

import mendes.sutil.dyego.awspresignedpost.conditions.key.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.conditions.key.KeyStartingWithCondition;
import mendes.sutil.dyego.awspresignedpost.conditions.key.KeyCondition;
import org.junit.jupiter.api.Test;

import static mendes.sutil.dyego.awspresignedpost.conditions.KeyConditionHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

class KeyConditionHelperTest {

  @Test
  public void shouldCreateExactKeyCondition() {
    // Arrange
    String keyValue = "my_file_name.txt";

    // Act
    KeyCondition keyCondition = withKey(keyValue);

    // Assert
    assertThat(keyCondition).isInstanceOf(ExactKeyCondition.class);
    assertThat(keyCondition.getValue()).isEqualTo(keyValue);
  }

  @Test
  public void shouldCreateStartWithKeyCondition() {
    // Arrange
    String value = "/user/leo";

    // Act
    KeyCondition startWithCondition = withKeyStartingWith(value);

    // Assert
    assertThat(startWithCondition).isInstanceOf(KeyStartingWithCondition.class);
    assertThat(startWithCondition.getValue()).isEqualTo(value);
  }

  @Test
  public void shouldCreateStartWithKeyConditionWithAnyKey() {
    // Act
    KeyCondition startWithCondition = withAnyKey();

    // Assert
    assertThat(startWithCondition).isInstanceOf(KeyStartingWithCondition.class);
    assertThat(startWithCondition.getValue()).isEqualTo("");
  }
}
