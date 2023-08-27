package io.github.dyegosutil.awspresignedpost.conditions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ContentLengthRangeConditionTest {

    @Test
    public void shouldTestContentLengthRangeCondition() {
        // Arrange
        ContentLengthRangeCondition metaCondition = new ContentLengthRangeCondition(50, 100);

        // Act
        String[] awsPolicyCondition = metaCondition.asAwsPolicyCondition();

        // Assert
        assertThat(awsPolicyCondition)
                .isEqualTo(new String[] {"content-length-range", "50", "100"});
        assertThat(metaCondition.getMinimumValue()).isEqualTo(50);
        assertThat(metaCondition.getMaximumValue()).isEqualTo(100);
    }
}
