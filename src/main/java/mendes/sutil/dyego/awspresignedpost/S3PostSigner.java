package mendes.sutil.dyego.awspresignedpost;

import java.util.Objects;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

public class S3PostSigner {

    private final AwsCredentials awsCredentials;

    S3PostSigner(AwsCredentialsProvider provider){
        this.awsCredentials = Objects.requireNonNull(
                provider.resolveCredentials(),
                "AwsCredentialsProvider must provide non-null AwsCredentials"
        );
    }

    public void createPresignedPost(PostRequestData request) {
//        val bucket = s3Properties.kycZipBucket
//        val key = "${personId.value}/$verificationId/kyc.zip"
//        val region = Region.EU_CENTRAL_1
//        val date = ZonedDateTime.now(clock)
//        String credentialsField = AwsSigner.buildCredentialField(awsCredentials, region, date)
        String credentialsField = "";


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
//        val signature = AwsSigner.hexDump(
//                AwsSigner.signMac(
//                        key = AwsSigner.generateSigningKey(
//                                secretKey = awsCredentials.secretAccessKey(),
//                                region = region,
//                                service = "s3",
//                                date = date
//                        ),
//                        data = policyB64.toByteArray(StandardCharsets.UTF_8)
//                )
//        )
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
    }

    //    public S3PostSigner(AwsCredentialsProvider credentialsProvider) {
//        mCredentialsProvider = credentialsProvider;
//    }

}
