package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import mendes.sutil.dyego.awspresignedpost.domain.Condition;

import java.util.List;

public abstract class RawCondition { // TODO REMOVE or Rename?

    /**
     * Value to be used while building the conditions.
     */
    private final String value;

    public RawCondition(String value) {
        this.value = value;
    }

    /**
     * @return The value to be used while building the conditions.
     */
    public String getValue() {
        return this.value;
    } // TODO remove?

    public abstract void addItselfTo(List<Condition> conditions);
}
