package mendes.sutil.dyego.awspresignedpost.conditions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorTest {

    @Test
    void shouldGetToStringValueFromOperator() {
        // Act
        MatchCondition.Operator exactCondition = MatchCondition.Operator.EQ;
        MatchCondition.Operator startsWithCondition = MatchCondition.Operator.STARTS_WITH;

        // Assert
        assertThat(exactCondition.toString()).isEqualTo("eq");
        assertThat(startsWithCondition.toString()).isEqualTo("starts-with");
    }
}