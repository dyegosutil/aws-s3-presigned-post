package mendes.sutil.dyego.awspresignedpost.conditions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition.Operator.EQ;
import static mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition.Operator.STARTS_WITH;
import static org.assertj.core.api.Assertions.assertThat;

class ConditionTest {

    @Test
    @DisplayName("Test uniqueness of ContentLengthRangeCondition")
    void testUniquenessOfContentLengthRangeCondition() {
        // Arrange
        Map<ContentLengthRangeCondition, String> map = new HashMap<>();

        // Act
        map.put(new ContentLengthRangeCondition(345, 345),"test");
        map.put(new ContentLengthRangeCondition(222, 222),"test2");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test uniqueness of MatchCondition")
    void testUniquenessOfMatchCondition() {
        // Arrange
        Map<MatchCondition, String> map = new HashMap<>();


        // Act
        map.put(new MatchCondition(ConditionField.CONTENT_TYPE, EQ, "test"),"test");
        map.put(new MatchCondition(ConditionField.CONTENT_TYPE, STARTS_WITH, "test2"),"test2");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test uniqueness of MetaCondition")
    void testUniquenessOfMetaCondition() {
        // Arrange
        Map<MetaCondition, String> map = new HashMap<>();

        // Act
        map.put(new MetaCondition(EQ, "myMeta","test"),"test");
        map.put(new MetaCondition(STARTS_WITH, "myMeta","test2"),"test");

        // Assert
        assertThat(map.size()).isEqualTo(1);
    }
}