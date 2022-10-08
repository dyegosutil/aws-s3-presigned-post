package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchConditionTest {

    @Test
    void toStringTest() {

        // Act
        MatchCondition.Match exactCondition = MatchCondition.Match.EQ;
        MatchCondition.Match startsWithCondition = MatchCondition.Match.STARTS_WITH;

        // Assert
        assertThat(exactCondition.toString()).isEqualTo("eq");
        assertThat(startsWithCondition.toString()).isEqualTo("starts-with");
    }
}