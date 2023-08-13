package mendes.sutil.dyego.awspresignedpost.conditions;

import org.junit.jupiter.api.Test;

import static mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition.Operator.EQ;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MetaConditionTest {

    @Test
    public void shouldGetAwsPolicyCondition() {
        // Arrange
        MetaCondition metaCondition = new MetaCondition(EQ, "myMetaName", "myMetaValue");

        // Act
        String[] awsPolicyCondition = metaCondition.asAwsPolicyCondition();

        // Assert
        assertThat(awsPolicyCondition).isEqualTo(new String[]{"eq","$x-amz-meta-myMetaName","myMetaValue"});
    }

    @Test
    public void shouldGetMetaName() {
        // Arrange
        MetaCondition metaCondition = new MetaCondition(EQ, "myMetaName", "myMetaValue");

        // Act
        String metaName = metaCondition.getMetaName();

        // Assert
        assertThat(metaName).isEqualTo("myMetaName");
    }

}