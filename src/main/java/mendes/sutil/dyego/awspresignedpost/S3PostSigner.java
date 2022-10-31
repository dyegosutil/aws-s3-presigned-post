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
    public PresignedPost create(PostParams postParams) {
        AmzDate amzDate = new AmzDate();
        Set<Condition> conditions = new HashSet<>(postParams.getConditions());
        addSessionToken(conditions);

        String bucket = postParams.getBucket();
        String region = postParams.getRegion().id();
        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com"; //TODO use string format
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
        String signature = produceSignature(postParams.getRegion(), amzDate, policyB64);

        return new PresignedPost(
                url,
                credentials,
                amzDate.formatForPolicy(),
                signature,
                policyB64, "AWS4-HMAC-SHA256" // Shoul it be a constant? but it is used only once
        );
    }

    public FreeTextPresignedPost create(FreeTextPostParams params) {
        AmzDate amzDate = new AmzDate(params.getDate());

        Policy policy = new Policy(
                params.getAmzExpirationDate().formatForPolicy(),
                params.getConditions()
        );
        final String policyJson = new Gson().toJson(policy);
        final String policyB64 = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        String signature = produceSignature(params.getRegion(), amzDate, policyB64);

        return new FreeTextPresignedPost(signature, policyB64);
    }

    private String produceSignature(Region region, AmzDate amzDate, String policyB64) {
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

    private void addSessionToken(Set<Condition> conditions) {
        if (awsCredentials instanceof AwsSessionCredentials) {
            conditions.add(new MatchCondition(SECURITY_TOKEN, EQ, ((AwsSessionCredentials) awsCredentials).sessionToken()));
        }
    }

    private Set<String[]> buildConditions(
            Set<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) { //TODO Two conditions? find a better name
        final Set<String[]> result = new HashSet<>();

        conditions.forEach(condition -> result.add(condition.asAwsPolicyCondition()));

        result.add(new String[]{"eq", ALGORITHM.awsConditionName, "AWS4-HMAC-SHA256"}); // TODO use EQ?
        result.add(new String[]{"eq", DATE.awsConditionName, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.awsConditionName, credentials});

        return result;
    }

    //    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose Set<String[]> conditions) {
    } // TODO should not this be a set
}
