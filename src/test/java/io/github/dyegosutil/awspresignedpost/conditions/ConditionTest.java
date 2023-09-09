package io.github.dyegosutil.awspresignedpost.conditions;

import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.EQ;
import static io.github.dyegosutil.awspresignedpost.conditions.MatchCondition.Operator.STARTS_WITH;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ConditionTest {

    @Test
    @DisplayName("Should test uniqueness of ContentLengthRangeCondition")
    void shouldTestUniquenessOfContentLengthRangeCondition() {
        // Arrange
        Map<ContentLengthRangeCondition, String> map = new HashMap<>();

        // Act
        map.put(new ContentLengthRangeCondition(345, 345), "test");
        map.put(new ContentLengthRangeCondition(222, 222), "test2");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should test uniqueness of MatchCondition")
    void shouldTestUniquenessOfMatchCondition() {
        // Arrange
        Map<MatchCondition, String> map = new HashMap<>();

        // Act
        map.put(new MatchCondition(ConditionField.CONTENT_TYPE, EQ, "test"), "test");
        map.put(new MatchCondition(ConditionField.CONTENT_TYPE, STARTS_WITH, "test2"), "test2");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should test uniqueness of MetaCondition")
    void shouldTestUniquenessOfMetaCondition() {
        // Arrange
        Map<MetaCondition, String> map = new HashMap<>();

        // Act
        map.put(new MetaCondition(EQ, "myMeta", "test"), "test");
        map.put(new MetaCondition(STARTS_WITH, "myMeta", "test2"), "test");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }
}
