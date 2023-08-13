package mendes.sutil.dyego.awspresignedpost.conditions;

/** Used to specify minimum and maximum size condition to accept the upload of a file */
public class ContentLengthRangeCondition extends Condition {

    private final long minimumValue;
    private final long maximumValue;

    public ContentLengthRangeCondition(long minimumValue, long maximumValue) {
        super(ConditionField.CONTENT_LENGTH_RANGE);
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public long getMinimumValue() {
        return minimumValue;
    }

    public long getMaximumValue() {
        return maximumValue;
    }

    @Override
    public String[] asAwsPolicyCondition() {
        return new String[] {
            getConditionField().valueForAwsPolicy,
            String.valueOf(getMinimumValue()),
            String.valueOf(getMaximumValue())
        };
    }
}
