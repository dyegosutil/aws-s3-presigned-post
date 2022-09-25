package mendes.sutil.dyego.awspresignedpost.domain.conditions.helper;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.KeyCondition;

/**
 * Provides helper methods to create instances of {@link KeyCondition}
 */
public class KeyConditionHelper {

    /**
     * Used to specify the exact s3 key name expected to be used for the upload
     *
     * @param keyValue The exact s3 key name expected to be used for the upload
     * @return An instance of {@link ExactKeyCondition}
     */
    public static KeyCondition withKey(String keyValue) {
        return new ExactKeyCondition(keyValue);
    }
}
