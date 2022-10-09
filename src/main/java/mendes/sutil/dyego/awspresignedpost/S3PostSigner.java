package mendes.sutil.dyego.awspresignedpost;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.MatchCondition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.ContentLengthRangeCondition;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
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
        String url = "https://"+bucket+".s3."+region+".amazonaws.com";
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

    //        val bucket = s3Properties.kycZipBucket
//        val key = "${personId.value}/$verificationId/kyc.zip"
//        val region = Region.EU_CENTRAL_1
//        String credentialsField = AwsSigner.buildCredentialField(awsCredentials, region, date)
//    String credentialsField = "";


//        val policy = Policy(
//                expiration = DateTimeFormatter.ISO_INSTANT.format(getTwoDaysInTheFuture()),
//                conditions = buildConditions(
//                        date = date,
//                        credentials = credentialsField,
//                        key = key,
//                        bucket = bucket,
//                        sessionToken = sessionToken
//                )
//        )
//        val policyJson = Gson().toJson(policy)
//        logger.debug { "Police document: $policyJson" }
//        val policyB64 = Base64.getEncoder().encodeToString(policyJson.toByteArray(StandardCharsets.UTF_8))
//

    //
//        return PreSignedPostDetails(
//                url = "https://$bucket.s3.eu-central-1.amazonaws.com",
//                key = key,
//                algorithm = "AWS4-HMAC-SHA256",
//                credential = credentialsField,
//                signature = signature,
//                date = AMZ_DATE_FORMATTER.format(date),
//                policy = policyB64
//        )


    private List<String[]> buildConditions(
            Set<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) { //TODO Two conditions? find a better name
        final List<String[]> result = new ArrayList<>();

        // double check it is continuing; after one condition
        conditions.forEach(condition -> {
            switch (condition.getConditionField()) { // TODO CHECK if continue keyword should be added to avoid checking useless conditions.
                case KEY -> result.add(new String[]{
                        ((MatchCondition) condition).getConditionMatch().awsOperatorValue,
                        condition.getConditionField().awsConditionName,
                        ((MatchCondition) condition).getValue()
                });
                case BUCKET -> result.add(new String[]{
                        ((MatchCondition) condition).getConditionMatch().awsOperatorValue,
                        condition.getConditionField().awsConditionName,
                        ((MatchCondition) condition).getValue()
                });
                case CONTENT_LENGTH_RANGE -> result.add(new String[]{
                        condition.getConditionField().awsConditionName,
                        String.valueOf(((ContentLengthRangeCondition) condition).getMinimumValue()),
                        String.valueOf(((ContentLengthRangeCondition) condition).getMaximumValue())
                });
                case CACHE_CONTROL, CONTENT_TYPE -> result.add(new String[]{
                        ((MatchCondition) condition).getConditionMatch().awsOperatorValue, // TODO perhaps change this to interfaces somehow object.getAwsOperatorValue or condition.addItselfToBuiltConditions
                        condition.getConditionField().awsConditionName,
                        ((MatchCondition) condition).getValue()
                });
            }
        });

        result.add(new String[]{"eq", ALGORITHM.awsConditionName, "AWS4-HMAC-SHA256"});
        result.add(new String[]{"eq", DATE.awsConditionName, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.awsConditionName, credentials});

        return result;
    }


//    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose List<String[]> conditions) { }
}
