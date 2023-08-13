package mendes.sutil.dyego.awspresignedpost.postparams;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TagTest {

  // Act
  @Test
  public void shouldTestTag() {

    // Act
    Tag tag = new Tag("testKey", "testValue");

    // Assert
    assertThat(tag.getKey()).isEqualTo("testKey");
    assertThat(tag.getValue()).isEqualTo("testValue");
  }
}
