package mendes.sutil.dyego.awspresignedpost;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import mendes.sutil.dyego.awspresignedpost.domain.AmzDate;
import mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import static mendes.sutil.dyego.awspresignedpost.domain.conditions.Condition.ConditionField.*;

public class S3PostSigner {
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
               DateTimeFormatter.ISO_INSTANT.format(postParams.getExpirationDate()),
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
            List<Condition> conditions,
            AmzDate xAmzDate,
            String credentials) { //TODO Two conditions? find a better name
        final List<String[]> result = new ArrayList<>();

        // double check it is continuing; after one condition
        conditions.forEach(condition -> {
            switch (condition.getConditionField()) {
                case KEY -> result.add(new String[]{   // TODO Should key be on the policy? see if not adding it here will work fine or not
                        "eq",
                        "$key",
                        condition.getValue()
                });
                case BUCKET -> result.add(new String[]{   // TODO Should key be on the policy? see if not adding it here will work fine or not
                        "eq",
                        "$bucket",
                        condition.getValue()
                });
//                case SUCCESS_ACTION_STATUS -> System.out.println();
            }
        });

        result.add(new String[]{"eq", ALGORITHM.name, "AWS4-HMAC-SHA256"});
        result.add(new String[]{"eq", DATE.name, xAmzDate.formatForPolicy()});
        result.add(new String[]{"eq", CREDENTIAL.name, credentials});

        return result;
//        final List<String[]> result = new ArrayList<>();
//        final Map<S3PostSignRequest.ConditionFields, Pair<String, S3PostSignRequest.ConditionMatch>> conditions = request.getConditions();
//
//        for (Map.Entry<S3PostSignRequest.ConditionFields, Pair<String, S3PostSignRequest.ConditionMatch>> item : conditions.entrySet()) {
//            switch (item.getKey()) {
//                case BUCKET:
//                case CREDENTIAL: {
//                    result.add(new String[] {
//                            "eq",
//                            item.getKey().name,
//                            item.getValue().getLeft()
//                    });
//                    break;
//                }
//                case KEY:
//                case CONTENT_TYPE:
//                case CONTENT_DISPOSITION:
//                case CONTENT_ENCODING:
//                case SUCCESS_ACTION_REDIRECT:
//                case SUCCESS_ACTION_STATUS: {
//                    result.add(new String[] {
//                            item.getValue().getRight().toString(),
//                            item.getKey().name,
//                            item.getValue().getLeft()
//                    });
//                    break;
//                }
//                case ACL: {
//                    break;
//                }
//            }
//        }
//
//        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.ALGORITHM.name, "AWS4-HMAC-SHA256"});
//        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.DATE.name, AMZ_DATE_FORMATTER.format(date)});
//        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.CREDENTIAL.name, credentials});
//
//        return result;
    }


//    @RequiredArgsConstructor
    private record Policy(@Expose String expiration, @Expose List<String[]> conditions) { }
}
