package mendes.sutil.dyego.awspresignedpost;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MetaCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import mendes.sutil.dyego.awspresignedpost.domain.response.FreeTextPresignedPost;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.*;
import static mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator.EQ;

public class S3PostSigner { // TODO rename?
    private final AwsCredentials awsCredentials;

    public S3PostSigner(AwsCredentialsProvider provider) { //TODO Perhaps change to not receive the params here but in the create part
        this.awsCredentials = Objects.requireNonNull(
                provider.resolveCredentials(),
                "AwsCredentialsProvider must provide non-null AwsCredentials"
        );
    }

    /**
     * Creates the Pre-Signed Post using the data provided in {@link  PostParams}
     * First the policy is created and then its base64 value is used to generate the signature using the
     * <a href="https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html">Aws Signature Version 4 specification</a>
     * <br><br>
     * This method performs several validations to prevent the generation of a faulty or invalid pre signed post
     * TODO Add validations: start-with and with, required/dependent conditions
     *
     * @param postParams Contains the information to be used to generate the pre signed post
     * @return The object containing all the necessary params to be used to upload a file using pre signed post
     */
    public PresignedPost create(PostParams postParams) {
        AmzDate amzDate = new AmzDate();
        Map<ConditionField, Condition> conditions = postParams.getConditions();
        addSessionTokenIfNeeded(conditions);

        String bucket = postParams.getBucket();
        String region = postParams.getRegion().id();
        String credentials = AwsSigner.buildCredentialField(awsCredentials, postParams.getRegion(), amzDate);

        Policy policy = new Policy(
                postParams.getAmzExpirationDate().formatForPolicy(),
                buildConditions(
                        conditions,
                        amzDate,
                        credentials
                )
        );
        final String policyJson = new Gson().toJson(policy);
        final String policyB64 = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        String signature = generateSignature(postParams.getRegion(), amzDate, policyB64);

        Map<String, String> returnConditions = keepOnlyNecessaryConditions(conditions);
        String keyUploadValue = getKeyUploadValue(returnConditions);
        removeKeyFromConditions(returnConditions);

        return new PresignedPost(
                createUrl(bucket,region),
                createConditionsMap(credentials,signature, amzDate, policyB64, keyUploadValue, returnConditions)
        );
    }

    private Map<String, String> createConditionsMap(
            String credentials,
            String signature,
            AmzDate amzDate,
            String policyB64,
            String keyUploadValue,
            Map<String, String> returnConditions) {
        Map<String,String> conditions = new HashMap<>();
        conditions.put(ConditionField.ALGORITHM.valueForApiCall, "AWS4-HMAC-SHA256");
        conditions.put(CREDENTIAL.valueForApiCall, credentials);
        conditions.put("x-amz-signature", signature);
        conditions.put(DATE.valueForApiCall, amzDate.formatForPolicy());
        conditions.put("policy", policyB64);
        conditions.put(KEY.valueForApiCall, keyUploadValue);
        conditions.putAll(returnConditions);
        return conditions;
    }

    private String createUrl(String bucket, String region) {
        return  "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private void removeKeyFromConditions(Map<String, String> filtered) {
        filtered.remove(KEY.valueForApiCall);
    }

    /**
     * TODO review?
     * Removes the {@link ConditionField#CONTENT_LENGTH_RANGE} and {@link ConditionField#BUCKET} since they are 
     * not necessary to be added in the client using the pre signed post.
     * 
     * @return A Map containing the condition key and value to be used in the upload. The value is returned as it is if 
     * the condition operator is {@link mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator#EQ}
     * or an empty string if the condition is 
     * {@link mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition.Operator#STARTS_WITH}, since the value
     * cannot be predicted.
     */
    private Map<String, String> keepOnlyNecessaryConditions(Map<ConditionField, Condition> conditionMap) {
        conditionMap.remove(CONTENT_LENGTH_RANGE);
        conditionMap.remove(BUCKET);

        return conditionMap
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> getUploadKey((MatchCondition) entry.getValue()),
                                entry -> getValueOrEmptyString((MatchCondition) entry.getValue())
                        )
                );
    }

    private String getUploadKey(MatchCondition matchCondition) {
        if (matchCondition instanceof MetaCondition metaCondition) {
            return metaCondition.getConditionField().valueForApiCall + metaCondition.getMetaName();
        }
        return matchCondition.getConditionField().valueForApiCall;
    }

    /**
     * In case an exact condition was used, the value so be sent to AWS S3 is known and thus can be returned. Otherwise,
     * a "start with" condition was used and therefore is not possible to foresee which value will be used by the
     * pre signed post caller
     *
     * @return The exact value to be used by the pre signed post caller or an empty string indicating that the caller
     * has to provide the data themselves.
     */
    private String getValueOrEmptyString(MatchCondition matchCondition) {
        if (matchCondition.getConditionOperator() == EQ) {
            return matchCondition.getValue();
        }
        return "";
    }

    /**
     * // TODO remove the "${filename}"
     */
    private String getKeyUploadValue(Map<String, String> returnConditions) {
        String keyValue = returnConditions.get(KEY.valueForApiCall);
        if (keyValue.isEmpty()) {
            return "${filename}";
        } else {
            return keyValue;
        }
    }

    /**
     * This method, compared to {@link #create(PostParams)}, gives more liberty to the caller who can provide more freely
     * the conditions to generate the pre signed post. Note that this method performs only basic validations hence its
     * use is more error-prone because the caller should know the intricacies of the
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html">Aws S3 Post Policy</a>.
     * This method might be useful though for using new features made available by AWS not yet added to
     * {@link #create(PostParams)} or for troubleshooting using raw data. For reference about how to use this method,
     * check the correspondent integration tests in the source code.
     * <br><br>
     * Creates the Pre-Signed Post using the data provided in {@link  FreeTextPostParams}
     * First the policy is created and then its base64 value is used to generate the signature using the
     * <a href="https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html">Aws Signature Version 4 specification</a>
     *
     * @param params Contains the information to be used to generate the pre signed post
     * @return The object containing only the signature and policy to be used to upload a file using pre signed post. Note
     * that these fields only are not enough to perform the file upload. The caller must add the other necessary fields
     * matching the conditions passed to this method.
     */
    public FreeTextPresignedPost create(FreeTextPostParams params) {
        AmzDate amzDate = new AmzDate(params.getDate());

        Policy policy = new Policy(
                params.getAmzExpirationDate().formatForPolicy(),
                params.getConditions()
        );
        final String policyJson = new Gson().toJson(policy);
        final String policyB64 = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        String signature = generateSignature(params.getRegion(), amzDate, policyB64);

        return new FreeTextPresignedPost(signature, policyB64);
    }

    private String generateSignature(Region region, AmzDate amzDate, String policyB64) {
        return AwsSigner.hexDump(
                AwsSigner.signMac(
                        AwsSigner.generateSigningKey(
                                awsCredentials.secretAccessKey(),
                                region,
                                amzDate
                        ),
                        policyB64.getBytes(StandardCharsets.UTF_8)
                )
        );
    }

    // TODO tel that token will be added auto
    private void addSessionTokenIfNeeded(Map<ConditionField, Condition> conditions) {
        if (awsCredentials instanceof AwsSessionCredentials) {
            conditions.put(
                    SECURITY_TOKEN,
                    new MatchCondition(SECURITY_TOKEN, EQ, ((AwsSessionCredentials) awsCredentials).sessionToken())
            );
        }
    }

    private Set<String[]> buildConditions(
            Map<ConditionField, Condition> conditions,
            AmzDate xAmzDate,
            String credentials) {
        final Set<String[]> result = new HashSet<>();

        conditions.forEach((key,condition)-> result.add(condition.asAwsPolicyCondition()));

        result.add(new String[]{"eq", ALGORITHM.valueForAwsPolicy, "AWS4-HMAC-SHA256"}); // TODO use EQ?
        result.add(new String[]{"eq", DATE.valueForAwsPolicy, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.valueForAwsPolicy, credentials});

        return result;
    }

    //    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose Set<String[]> conditions) { // record TODO does not work with java8, change it
    } // TODO should not this be a set
}
