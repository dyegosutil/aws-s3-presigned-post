package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionMatchTest {

    @Test
    void toStringTest() {

        // Act
        Condition.ConditionMatch exactCondition = Condition.ConditionMatch.EQ;
        Condition.ConditionMatch startsWithCondition = Condition.ConditionMatch.STARTS_WITH;

        // Assert
        assertThat(exactCondition.toString()).isEqualTo("eq");
        assertThat(startsWithCondition.toString()).isEqualTo("starts-with");
    }
}