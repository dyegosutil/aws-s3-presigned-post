package mendes.sutil.dyego.awspresignedpost.domain.conditions;

// TODO
public class StartWithKeyCondition extends KeyCondition{

    public StartWithKeyCondition(String expectedKeyStartingWith) {
        super(expectedKeyStartingWith);
    }
}