package mendes.sutil.dyego.awspresignedpost;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import mendes.sutil.dyego.awspresignedpost.domain.response.FreeTextPresignedPost;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    // TODO rename to sign, or sign post??
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
        Set<Condition> conditions = new HashSet<>(postParams.getConditions());
        addSessionTokenIfNeeded(conditions);

        String bucket = postParams.getBucket();
        String region = postParams.getRegion().id();
        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com";
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

        return new PresignedPost(
                url,
                credentials,
                amzDate.formatForPolicy(),
                signature,
                policyB64, "AWS4-HMAC-SHA256" // Shoul it be a constant? but it is used only once
        );
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

    private void addSessionTokenIfNeeded(Set<Condition> conditions) {
        if (awsCredentials instanceof AwsSessionCredentials) {
            conditions.add(new MatchCondition(SECURITY_TOKEN, EQ, ((AwsSessionCredentials) awsCredentials).sessionToken()));
        }
    }

    private Set<String[]> buildConditions(
            Set<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) {
        final Set<String[]> result = new HashSet<>();

        conditions.forEach(condition -> result.add(condition.asAwsPolicyCondition()));

        result.add(new String[]{"eq", ALGORITHM.awsConditionName, "AWS4-HMAC-SHA256"}); // TODO use EQ?
        result.add(new String[]{"eq", DATE.awsConditionName, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.awsConditionName, credentials});

        return result;
    }

    //    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose Set<String[]> conditions) { // record TODO does not work with java8, change it
    } // TODO should not this be a set
}
