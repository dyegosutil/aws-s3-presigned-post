package mendes.sutil.dyego.awspresignedpost.domain.conditions;

import mendes.sutil.dyego.awspresignedpost.domain.Condition;

import java.util.List;

// TODO
public class StartWithKeyCondition extends KeyCondition{

    public StartWithKeyCondition(String expectedKeyStartingWith) {
        super(expectedKeyStartingWith);
    }

    @Override
    public void addItselfTo(List<Condition> conditions) {

    }
}