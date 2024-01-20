package io.github.dyegosutil.awspresignedpost.conditions;

import io.github.dyegosutil.awspresignedpost.conditions.key.ExactKeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyCondition;
import io.github.dyegosutil.awspresignedpost.conditions.key.KeyStartingWithCondition;

/** Provides util methods to create instances of {@link KeyCondition} */
public class KeyConditionUtil {

    private KeyConditionUtil() {}

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
     * Used to specify how the s3 key should start. Note that it allows the pre-signed post user to
     * specify as many folders as they want after the starting value chosen here and then finally
     * set the key name.
     *
     * @param value The value which the key should start with when provided by the pre-signed post
     *     user
     * @return {@link KeyStartingWithCondition}
     */
    public static KeyStartingWithCondition withKeyStartingWith(String value) {
        return new KeyStartingWithCondition(value);
    }

    /**
     * Allow any key to be specified by the pre-signed post user.
     *
     * @return {@link KeyStartingWithCondition}
     */
    public static KeyStartingWithCondition withAnyKey() {
        return new KeyStartingWithCondition("");
    }
}
