package mendes.sutil.dyego.awspresignedpost;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.ConditionField.*;

public class S3PostSigner { // TODO rename?
    private final AwsCredentials awsCredentials;

    S3PostSigner(AwsCredentialsProvider provider) {
        this.awsCredentials = Objects.requireNonNull(
                provider.resolveCredentials(),
                "AwsCredentialsProvider must provide non-null AwsCredentials"
        );
    }

    public PresignedPost create(PostParams postParams) {
        AmzDate amzDate = new AmzDate();

        String bucket = postParams.getBucket();
        String region = postParams.getRegion().id();
        String url = "https://"+bucket+".s3."+region+".amazonaws.com"; // TODO use string format
        String credentials = AwsSigner.buildCredentialField(awsCredentials, postParams.getRegion(), amzDate);

        Policy policy = new Policy(
                postParams.getAmzExpirationDate().formatForPolicy(),
                buildConditions(
                        postParams.getConditions(),
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

    private List<String[]> buildConditions(
            Set<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) { //TODO Two conditions? find a better name
        final List<String[]> result = new ArrayList<>();

        conditions.forEach(condition -> result.add(condition.asAwsPolicyCondition()));

        result.add(new String[]{"eq", ALGORITHM.awsConditionName, "AWS4-HMAC-SHA256"});
        result.add(new String[]{"eq", DATE.awsConditionName, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.awsConditionName, credentials});

        return result;
    }


//    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose List<String[]> conditions) { }
}
