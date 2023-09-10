package io.github.dyegosutil.awspresignedpost.postparams;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

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
