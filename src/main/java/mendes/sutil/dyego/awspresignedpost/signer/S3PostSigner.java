package mendes.sutil.dyego.awspresignedpost.signer;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.AmzDate;
import mendes.sutil.dyego.awspresignedpost.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.conditions.ConditionField;
import mendes.sutil.dyego.awspresignedpost.conditions.MetaCondition;
import mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition;
import mendes.sutil.dyego.awspresignedpost.presigned.PresignedFreeTextPost;
import mendes.sutil.dyego.awspresignedpost.presigned.PreSignedPost;
import mendes.sutil.dyego.awspresignedpost.postparams.FreeTextPostParams;
import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static mendes.sutil.dyego.awspresignedpost.conditions.ConditionField.*;
import static mendes.sutil.dyego.awspresignedpost.conditions.MatchCondition.Operator.EQ;

public final class S3PostSigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3PostSigner.class);

    /**
     * Creates the Pre-Signed Post using the data provided in {@link PostParams}. First the policy is
     * created and then its base64 value is used to generate the signature using the <a
     * href="https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html">Aws Signature Version
     * 4 specification</a> <br>
     * <br>
     * This method performs several validations to prevent the generation of a faulty or invalid pre
     * signed post
     *
     * @param postParams Contains the conditions to be used to generate the pre signed post
     * @param awsCredentialsProvider Contains the AWS credentials to be used to generate the pre
     *     signed post. If session credentials are received, the methods adds the {@link
     *     ConditionField#SECURITY_TOKEN} to the pre signed post
     * @return The object containing all the necessary params to be used to upload a file using pre
     *     signed post
     */
    public static PreSignedPost sign(
            final PostParams postParams, final AwsCredentialsProvider awsCredentialsProvider) {
        requireNonNull(postParams, "PostParam cannot be null");
        final AwsCredentials awsCredentials = validateAwsCredentials(awsCredentialsProvider);

        final AmzDate amzDate = getAmzDate();
        Map<ConditionField, Condition> conditions = getConditions(postParams);
        addSessionTokenIfNeeded(conditions, awsCredentials);

        final String bucket = postParams.getBucket();
        final String region = postParams.getRegion().id();
        final String credentials =
                buildCredentialField(awsCredentials, postParams.getRegion(), amzDate);

        final Policy policy =
                new Policy(
                        postParams.getAmzExpirationDate().formatForPolicy(),
                        buildConditions(conditions, amzDate, credentials));
        final String policyJson = new Gson().toJson(policy);
        LOGGER.debug("PolicyJson: {}", policyJson);
        final String policyB64 =
                Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        final String signature =
                generateSignature(postParams.getRegion(), amzDate, policyB64, awsCredentials);

        Map<String, String> returnConditions = keepOnlyNecessaryConditions(conditions);
        final String keyUploadValue = getKeyUploadValue(returnConditions);
        removeKeyFromConditions(returnConditions);

        return new PreSignedPost(
                createUrl(bucket, region),
                createConditionsMap(
                        credentials,
                        signature,
                        amzDate,
                        policyB64,
                        keyUploadValue,
                        returnConditions));
    }

    private static Map<ConditionField, Condition> getConditions(PostParams postParams) {
        Map<ConditionField, Condition> conditions = postParams.getConditions();
        LOGGER.debug(
                "Conditions to generate pre signed post {}", concatenateConditionField(conditions));
        return conditions;
    }

    private static AmzDate getAmzDate() {
        AmzDate amzDate = new AmzDate();
        LOGGER.debug("Date used to generate pre signed post {}", amzDate.formatForPolicy());
        return amzDate;
    }

    /**
     * This method, compared to {@link #sign(PostParams, AwsCredentialsProvider)}, gives more
     * liberty to the caller who can provide more freely the conditions to generate the pre signed
     * post. Note that this method performs only basic validations hence its use is more error-prone
     * because the caller should know the intricacies of the <a
     * href="https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html">Aws
     * S3 Post Policy</a>. This method might be useful though for using new features made available
     * by AWS not yet added to {@link #sign(PostParams, AwsCredentialsProvider)} or for
     * troubleshooting using raw data. For reference about how to use this method, check the
     * correspondent integration tests in the source code. <br>
     * <br>
     * Creates the Pre-Signed Post using the data provided in {@link FreeTextPostParams} First the
     * policy is created and then its base64 value is used to generate the signature using the <a
     * href="https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html">Aws Signature Version
     * 4 specification</a>
     *
     * @param params Contains the information to be used to generate the pre signed post
     * @return The object containing only the signature and policy to be used to upload a file using
     *     pre signed post. Note that these fields only are not enough to perform the file upload.
     *     The caller must add the other necessary fields matching the conditions passed to this
     *     method.
     */
    public static PresignedFreeTextPost sign(
            final FreeTextPostParams params, final AwsCredentialsProvider provider) {
        final AwsCredentials awsCredentials = validateAwsCredentials(provider);
        final AmzDate amzDate = new AmzDate(params.getDate());

        final Policy policy =
                new Policy(params.getAmzExpirationDate().formatForPolicy(), params.getConditions());
        final String policyJson = new Gson().toJson(policy);
        final String policyB64 =
                Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        final String signature =
                generateSignature(params.getRegion(), amzDate, policyB64, awsCredentials);

        return new PresignedFreeTextPost(signature, policyB64);
    }

    private static AwsCredentials validateAwsCredentials(AwsCredentialsProvider provider) {
        return requireNonNull(
                provider.resolveCredentials(),
                "AwsCredentialsProvider must provide non-null AwsCredentials");
    }

    public static String buildCredentialField(
            final AwsCredentials credentials, final Region region, final AmzDate amzDate) {
        final String accessKeyId = credentials.accessKeyId();
        final String regionId = region.id();
        final String date = amzDate.formatForCredentials();
        return accessKeyId + "/" + date + "/" + regionId + "/s3/aws4_request";
    }

    private static String concatenateConditionField(Map<ConditionField, Condition> condition) {
        return condition.keySet().stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static Map<String, String> createConditionsMap(
            final String credentials,
            final String signature,
            final AmzDate amzDate,
            final String policyB64,
            final String keyUploadValue,
            final Map<String, String> returnConditions) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put(ConditionField.ALGORITHM.valueForApiCall, "AWS4-HMAC-SHA256");
        conditions.put(CREDENTIAL.valueForApiCall, credentials);
        conditions.put("x-amz-signature", signature);
        conditions.put(DATE.valueForApiCall, amzDate.formatForPolicy());
        conditions.put("policy", policyB64);
        conditions.put(KEY.valueForApiCall, keyUploadValue);
        conditions.putAll(returnConditions);
        return conditions;
    }

    private static String createUrl(final String bucket, final String region) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private static void removeKeyFromConditions(Map<String, String> filtered) {
        filtered.remove(KEY.valueForApiCall);
    }

    /**
     * Removes the {@link ConditionField#CONTENT_LENGTH_RANGE} and {@link ConditionField#BUCKET}
     * since they are not necessary to be added in the client using the pre signed post.
     *
     * @return A Map containing the condition key and value to be used in the upload. The value is
     *     returned as it is if the condition operator is {@link MatchCondition.Operator#EQ} or an
     *     empty string if the condition is {@link MatchCondition.Operator#STARTS_WITH}, since the
     *     value cannot be predicted.
     */
    private static Map<String, String> keepOnlyNecessaryConditions(
            Map<ConditionField, Condition> conditionMap) {
        conditionMap.remove(CONTENT_LENGTH_RANGE);
        conditionMap.remove(BUCKET);

        return conditionMap.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> getUploadKey((MatchCondition) entry.getValue()),
                                entry -> getValueOrEmptyString((MatchCondition) entry.getValue())));
    }

    private static String getUploadKey(MatchCondition matchCondition) {
        if (matchCondition instanceof MetaCondition) {
            MetaCondition metaCondition = (MetaCondition) matchCondition;
            return metaCondition.getConditionField().valueForApiCall + metaCondition.getMetaName();
        }
        return matchCondition.getConditionField().valueForApiCall;
    }

    /**
     * In case an exact condition was used, the value so be sent to AWS S3 is known and thus can be
     * returned. Otherwise, a "start with" condition was used and therefore is not possible to
     * foresee which value will be used by the pre signed post caller
     *
     * @return The exact value to be used by the pre signed post caller or an empty string
     *     indicating that the caller has to provide the data themselves.
     */
    private static String getValueOrEmptyString(MatchCondition matchCondition) {
        if (matchCondition.getConditionOperator() == EQ) {
            return matchCondition.getValue();
        }
        return "";
    }

    private static String getKeyUploadValue(Map<String, String> returnConditions) {
        return returnConditions.get(KEY.valueForApiCall);
    }

    private static String generateSignature(
            final Region region,
            final AmzDate amzDate,
            final String policyB64,
            final AwsCredentials awsCredentials) {
        return AwsSigner.hexDump(
                AwsSigner.signMac(
                        AwsSigner.generateSigningKey(
                                awsCredentials.secretAccessKey(), region, amzDate),
                        policyB64.getBytes(StandardCharsets.UTF_8)));
    }

    private static void addSessionTokenIfNeeded(
            Map<ConditionField, Condition> conditions, final AwsCredentials awsCredentials) {
        if (awsCredentials instanceof AwsSessionCredentials) {
            LOGGER.debug(
                    "Adding {} since Aws Session credential is being used", SECURITY_TOKEN.name());
            conditions.put(
                    SECURITY_TOKEN,
                    new MatchCondition(
                            SECURITY_TOKEN,
                            EQ,
                            ((AwsSessionCredentials) awsCredentials).sessionToken()));
        }
    }

    private static Set<String[]> buildConditions(
            Map<ConditionField, Condition> conditions, AmzDate xAmzDate, String credentials) {
        final Set<String[]> result = new HashSet<>();

        conditions.forEach((key, condition) -> result.add(condition.asAwsPolicyCondition()));

        result.add(
                new String[] {
                    EQ.awsOperatorValue, ALGORITHM.valueForAwsPolicy, "AWS4-HMAC-SHA256"
                });
        result.add(
                new String[] {
                    EQ.awsOperatorValue, DATE.valueForAwsPolicy, xAmzDate.formatForPolicy()
                });
        result.add(new String[] {EQ.awsOperatorValue, CREDENTIAL.valueForAwsPolicy, credentials});

        return result;
    }

    private static class Policy {
        @SuppressWarnings("unused")
        @Expose
        private String expiration;

        @SuppressWarnings("unused")
        @Expose
        private Set<String[]> conditions;

        public Policy(String expiration, Set<String[]> conditions) {
            this.expiration = expiration;
            this.conditions = conditions;
        }
    }
}
