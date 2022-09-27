package mendes.sutil.dyego.awspresignedpost.domain.conditions.helper;

import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.ExactKeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.key.KeyStartsWithCondition;

/**
 * Provides helper methods to create instances of {@link KeyCondition}
 */
public class KeyConditionHelper {

    /**
     * Used to specify the exact s3 key name expected to be used for the upload
     *
     * @param keyValue The exact s3 key name expected to be used for the upload
     * @return {@link ExactKeyCondition}
     */
    public static ExactKeyCondition withKey(String keyValue) {
        return new ExactKeyCondition(keyValue);
    }

    /**
     * Used to specify how the s3 key should start.
     * Warning: Note that it allows the pre-signed post user to specify as many folders as they want after the starting
     * value chosen here and then finally set the key name.
     *
     * @param value The value which the key should start with when provided by the pre-signed post user
     * @return @link KeyStartsWithCondition}
     */
    public static KeyCondition witKeyStartingWith(String value) {
        return new KeyStartsWithCondition(value);
    }

    /**
     * Allow any key to be specified by the pre-signed post user.
     * If the value provided by the user is for the param 'key' is ${filename}, the name of the file being uploaded will
     * be used.
     * @return {@link KeyStartsWithCondition}
     */
    public static KeyStartsWithCondition withAnyKey() {
        return new KeyStartsWithCondition("");
    }
}
