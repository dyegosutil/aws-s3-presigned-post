package mendes.sutil.dyego.awspresignedpost;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
        String url = "https://"+bucket+".s3."+region+".amazonaws.com"; //TODO use string format
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

        String signature = AwsSigner.hexDump(
                AwsSigner.signMac(
                        AwsSigner.generateSigningKey(
                                awsCredentials.secretAccessKey(),
                                postParams.getRegion(),
                                amzDate
                        ),
                        policyB64.getBytes(StandardCharsets.UTF_8)
                )
        );

        return new PresignedPost(
                url,credentials, amzDate.formatForPolicy(), signature, policyB64, "AWS4-HMAC-SHA256"
        );
    }

    private void addSessionToken(Set<Condition> conditions) {
        if(awsCredentials instanceof AwsSessionCredentials) {
            conditions.add(new MatchCondition(SECURITY_TOKEN, EQ, ((AwsSessionCredentials) awsCredentials).sessionToken()));
        }
    }

    private List<String[]> buildConditions(
            Set<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) { //TODO Two conditions? find a better name
        final List<String[]> result = new ArrayList<>();

        conditions.forEach(condition -> result.add(condition.asAwsPolicyCondition()));

        result.add(new String[]{"eq", ALGORITHM.awsConditionName, "AWS4-HMAC-SHA256"}); // TODO use EQ?
        result.add(new String[]{"eq", DATE.awsConditionName, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.awsConditionName, credentials});

        return result;
    }


//    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose List<String[]> conditions) { }
}
