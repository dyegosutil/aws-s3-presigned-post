package mendes.sutil.dyego.awspresignedpost.integrationtests.accesskey.akia;

import mendes.sutil.dyego.awspresignedpost.integrationtests.accesskey.IntegrationTests;
import mendes.sutil.dyego.awspresignedpost.postparams.PostParams;
import mendes.sutil.dyego.awspresignedpost.presigned.PreSignedPost;
import mendes.sutil.dyego.awspresignedpost.signer.S3PostSigner;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.postparams.PostParams.Builder.CannedAcl.PRIVATE;
import static mendes.sutil.dyego.awspresignedpost.postparams.PostParams.Builder.StorageClass.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class PreSignedPostOptionalPostParamsIntegrationTests extends IntegrationTests {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PreSignedPostOptionalPostParamsIntegrationTests.class);

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_SESSION_TOKEN", null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("optionalPostParamsTestCases")
    void shouldTestUploadWithOptionalParams(String testDescription, PostParams postParams) {
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
        Request request =
                createRequestFromConditions(presignedPost.getConditions(), presignedPost.getUrl());
        boolean result = postFileIntoS3(request);
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("OptionalCustomizedPostParamsTestCases")
    void shouldTestUploadWithCustomizedOptionalParams(
            String testDescription,
            PostParams postParams,
            Map<String, String> customizedUploadConditions,
            boolean expectedResult) {
        // Arrange
        PreSignedPost presignedPost = S3PostSigner.sign(postParams);
        Map<String, String> conditions = presignedPost.getConditions();
        conditions.putAll(customizedUploadConditions);
        Request request =
                createRequestFromConditions(presignedPost.getConditions(), presignedPost.getUrl());

        // Act
        boolean result = postFileIntoS3(request);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> optionalPostParamsTestCases() {
        String tagging =
                "<Tagging><TagSet><Tag><Key>MyTestTag</Key><Value>MyTagValue</Value></Tag></TagSet></Tagging>";
        return Stream.of(
                // content-length-range
                of(
                        "Should succeed while uploading file to S3 when it's size is between the"
                                + " minimum and maximum specified values in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withContentLengthRange(7, 20)
                                .build()),
                // content-length-range
                of(
                        "Should succeed while uploading file to S3 when it's size is of the exact"
                                + " size specified values in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withContentLengthRange(14, 14)
                                .build()),
                // Cache-Control
                of(
                        "Should succeed while uploading file to S3 when the cache-control specified"
                                + " is the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withCacheControl("public, max-age=7200")
                                .build()),
                // Content-Type
                of(
                        "Should succeed while uploading file to S3 when the exact Content-Type"
                                + " specified is the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withContentType("text/plain")
                                .build()),
                // Content Disposition
                of(
                        "Should succeed while uploading file to S3 using the exact content"
                                + " disposition set in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withContentDisposition("inline")
                                .build()),
                // Content-Encoding
                of(
                        "Should succeed while uploading file to S3 using the exact content encoding"
                                + " set in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withContentEncoding("compress")
                                .build()),
                // Expires
                of(
                        "Should succeed while uploading file to S3 using the exact Expires"
                                + " condition set in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        null,
                        true),
                // acl
                of(
                        "Should succeed while uploading file to S3 when the acl specified is the"
                                + " same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey().withAcl(PRIVATE).build()),
                // tagging
                of(
                        "Should succeed while uploading file to S3 when it's free text tagging"
                                + " value is the same as the one specified in the policy",
                        createDefaultPostParamBuilderSpecifyingKey().withTagging(tagging).build()),
                // tagging
                of(
                        "Should succeed while uploading file to S3 when it's tagging is the same as"
                                + " the one specified in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withTag("myTagKey", "myTagValue")
                                .withTag("myTagKey2", "myTagValue2")
                                .build()),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the 1 meta specified is the"
                                + " same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .build()),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the 2 metas specified are"
                                + " the same as the ones in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .withMeta("my_meta_data2", "value for my meta-data2")
                                .build()),

                // storage-class
                of(
                        "Should succeed while uploading file to S3 when the storage class specified"
                                + " is the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withStorageClass(STANDARD)
                                .build()),
                // website-redirect-location
                of(
                        "Should succeed while uploading file to S3 when the"
                            + " website-redirect-location specified is the same as the one in the"
                            + " policy which redirects to a file in the same bucket",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withWebsiteRedirectLocation("/anotherPage.html")
                                .build()),
                // website-redirect-location
                of(
                        "Should succeed while uploading file to S3 when the"
                            + " website-redirect-location specified is the same as the one in the"
                            + " policy which redirects to another website",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withWebsiteRedirectLocation("https://www.google.com")
                                .build()),
                // checksum-sha256
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is"
                            + " SHA256, the checksum specified is the same as the one in the policy"
                            + " and the checksum is the same as the one generated by aws",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withChecksumSha256(generateChecksumSha256Base64Encoded())
                                .build()),
                // checksum-sha1
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is"
                            + " SHA1, the checksum specified is the same as the one in the policy"
                            + " and the checksum is the same as the one generated by aws",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withChecksumSha1(generateChecksumSha1Base64Encoded())
                                .build()),
                // checksum-CRC32
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is"
                            + " CRC32, the checksum specified is the same as the one in the policy"
                            + " and the checksum is the same as the one generated by aws",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withChecksumCrc32("DR7n6g==")
                                .build()),
                // checksum-CRC32C
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is"
                            + " CRC32C, the checksum specified is the same as the one in the policy"
                            + " and the checksum is the same as the one generated by aws",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withChecksumCrc32c("fPxmpw==")
                                .build()),

                // x-amz-server-side-encryption
                of(
                        "Should succeed while uploading file to S3 when the server-side-encryption"
                                + " specified is AWS_KMS and is the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .build()),
                // x-amz-server-side-encryption
                of(
                        "Should succeed while uploading file to S3 when the server-side-encryption"
                                + " specified is AES256 and is the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AES256)
                                .build()),
                // server-side-encryption-aws-kms-key-id
                of(
                        "Should succeed while uploading file to S3 when the"
                            + " server-side-encryption-aws-kms-key-id specified is the same as the"
                            + " one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionAwsKmsKeyId(
                                        System.getenv("AWS_KMS_S3_KEY"))
                                .build()),
                // server-side-encryption-context
                of(
                        "Should succeed while uploading file to S3 when the base64 encoded json"
                            + " server-side-encryption-context specified is the same as the one in"
                            + " the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionContext("ewogICJ0ZXN0IjogInRlc3QiCn0=")
                                .build()),
                // server-side-encryption-bucket-key-enabled
                of(
                        "Should succeed while uploading file to S3 when the"
                            + " server-side-encryption-bucket-key-enabled set as true specified is"
                            + " the same as the one in the policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionBucketKeyEnabled()
                                .build()),
                // x-amz-server-side-encryption-customer-algorithm
                // x-amz-server-side-encryption-customer-key
                // x-amz-server-side-encryption-customer-key-MD5
                of(
                        "Should succeed while uploading file to S3 when customer-provided"
                                + " encryption conditions specified are the same as the ones in the"
                                + " policy",
                        createDefaultPostParamBuilderSpecifyingKey()
                                .withServerSideEncryptionCustomerAlgorithmAES256()
                                .withServerSideEncryptionCustomerKey(
                                        encodeToBase64(encryptionKey256bits))
                                .withServerSideEncryptionCustomerKeyMD5(
                                        generateEncryptionKeyMD5DigestAsBase64())
                                .build()));
    }

    private static Stream<Arguments> OptionalCustomizedPostParamsTestCases() {
        return Stream.of(
                // content-length-range
                of(
                        "Should fail while uploading file to S3 when it's size is over the maximum"
                                + " specified value in the policy",
                        createDefaultPostParamBuilder().withContentLengthRange(1, 2).build(),
                        createFormDataParts("key", "${filename}"),
                        false),
                // content-length-range
                of(
                        "Should fail while uploading file to S3 when it's size is under the minimum"
                                + " specified value in the policy",
                        createDefaultPostParamBuilder().withContentLengthRange(15, 20).build(),
                        createFormDataParts("key", "${filename}"),
                        false),
                // Cache-Control
                of(
                        "Should succeed while uploading file to S3 when the cache-control specified"
                                + " starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControlStartingWith("public,")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "Cache-Control", "public, max-age=7200"),
                        true),
                // Cache-Control
                of(
                        "Should fail while uploading file to S3 when the cache-control specified is"
                                + " not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "Cache-Control", "public, max-age=7201"),
                        false),
                // Cache-Control
                of(
                        "Should fail while uploading file to S3 when the cache-control specified"
                                + " does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "Cache-Control", "public, max-age=7201"),
                        false),
                // Content-Type
                of(
                        "Should succeed while uploading file to S3 when the Content-Type specified"
                                + " starts with the same value specified in the policy",
                        createDefaultPostParamBuilder().withContentTypeStartingWith("tex").build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        true),
                // Content-Type
                of(
                        "Should fail while uploading file to S3 when the exact Content-Type"
                                + " specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder().withContentType("text/plain").build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "Aext/plain"),
                        false),
                // Content-Type
                of(
                        "Should fail while uploading file to S3 when the Content-Type specified"
                                + " does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder().withContentTypeStartingWith("dex").build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        false),
                // Content Disposition
                of(
                        "Should succeed while uploading file to S3 using the content disposition"
                                + " starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inli")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "inline"),
                        true),
                // Content Disposition
                of(
                        "Should fail while uploading file to S3 not using the exact content"
                                + " disposition set in the policy",
                        createDefaultPostParamBuilder().withContentDisposition("inline").build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false),
                // Content Disposition
                of(
                        "Should fail while uploading file to S3 using the content disposition"
                                + " starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false),
                // Content-Encoding
                of(
                        "Should succeed while uploading file to S3 using the content encoding"
                                + " starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "compress"),
                        true),
                // Content-Encoding
                of(
                        "Should fail while uploading file to S3 not using the exact content"
                                + " encoding set in the policy",
                        createDefaultPostParamBuilder().withContentEncoding("compress").build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "gzip"),
                        false),
                // Content-Encoding
                of(
                        "Should fail while uploading file to S3 using the content encoding starting"
                                + " with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "abc"),
                        false),
                // Expires
                of(
                        "Should succeed while uploading file to S3 using the Expires condition"
                                + " starting with value as set in the policy",
                        createDefaultPostParamBuilder().withExpiresStartingWith("Wed,").build(),
                        createFormDataPartsWithKeyCondition(
                                "Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        true),
                // Expires
                of(
                        "Should fail while uploading file to S3 not using the exact Expires"
                                + " condition set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        false),
                // Expires
                of(
                        "Should fail while uploading file to S3 using the Expires starting with"
                                + " value different than the one set in the policy",
                        createDefaultPostParamBuilder().withExpiresStartingWith("Wed,").build(),
                        createFormDataPartsWithKeyCondition(
                                "Expires", "Mon, 21 Oct 2015 07:29:00 GMT"),
                        false),
                // acl
                of(
                        "Should succeed while uploading file to S3 when the acl starts with value"
                                + " specified is the same as the one in the policy",
                        createDefaultPostParamBuilder().withAclStartingWith("pri").build(),
                        createFormDataPartsWithKeyCondition("acl", "private"),
                        true),
                // acl
                of(
                        "Should fail while uploading file to S3 when the acl starts with value"
                                + " specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder().withAclStartingWith("abc").build(),
                        createFormDataPartsWithKeyCondition("acl", "private"),
                        false),
                // acl
                of(
                        "Should fail while uploading file to S3 when the acl specified is not the"
                                + " same as the one in the policy",
                        createDefaultPostParamBuilder().withAcl(PRIVATE).build(),
                        createFormDataPartsWithKeyCondition("acl", "wrongValue"),
                        false),
                // tagging
                of(
                        "Should fail while uploading file to S3 when it's free text tagging is the"
                                + " same as the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTagging(
                                        "<Tagging><TagSet><Tag><Key>MyTestTag</Key><Value>MyTagValue</Value></Tag></TagSet></Tagging>")
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", "wrongValue"),
                        false),
                // tagging
                of(
                        "Should fail while uploading file to S3 when it's tagging is the same as"
                                + " the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTag("myTagKey", "myTagValue")
                                .withTag("myTagKey2", "myTagValue2")
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", "wrongValue"),
                        false),
                // meta
                of(
                        "Should fail while uploading file to S3 when the meta specified is the same"
                                + " as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data", "not my meta"),
                        false),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the meta starting value is"
                                + " the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMetaStartingWith("my_meta_data", "abcde")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-meta-my_meta_data", "abcdefg"),
                        true),
                // meta
                of(
                        "Should fail while uploading file to S3 when the meta starting value is not"
                                + " the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMetaStartingWith("my_meta_data", "abcde")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-meta-my_meta_data", "xyz"),
                        false),
                // storage-class
                of(
                        "Should fail while uploading file to S3 when the storage class specified is"
                                + " not the same as the one in the policy",
                        createDefaultPostParamBuilder().withStorageClass(STANDARD).build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-storage-class", "INTELLIGENT_TIERING"),
                        false),
                // website-redirect-location
                of(
                        "Should fail while uploading file to S3 when the website-redirect-location"
                                + " specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withWebsiteRedirectLocation("/anotherPage.html")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-website-redirect-location", "/yetAnotherPage.html"),
                        false),
                // checksum-sha256
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " SHA256, the checksum specified is not the same as the one in the"
                            + " policy and the checksum is not the same as the one generated by"
                            + " aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha256(generateChecksumSha256Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-checksum-sha256", "wrongChecksum"),
                        false),
                // checksum-sha256
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " SHA256, the checksum specified is the same as the one in the policy"
                            + " but the checksum is not the same as the one generated by aws",
                        createDefaultPostParamBuilder().withChecksumSha256("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-checksum-sha256", "wrongChecksum"),
                        false),
                // checksum-sha1
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                                + " SHA1, the checksum specified is not the same as the one in the"
                                + " policy and the checksum is not the same as the one generated by"
                                + " aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha1(generateChecksumSha1Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha1", "wrongChecksum"),
                        false),
                // checksum-sha1
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " SHA1, the checksum specified is the same as the one in the policy"
                            + " but the checksum is not the same as the one generated by aws",
                        createDefaultPostParamBuilder().withChecksumSha1("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha1", "wrongChecksum"),
                        false),
                // checksum-CRC32
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                                + " CRC32, the checksum specified is not the same as the one in the"
                                + " policy and the checksum is not the same as the one generated by"
                                + " aws",
                        createDefaultPostParamBuilder().withChecksumCrc32("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32", "DR7n6g=="),
                        false),
                // checksum-CRC32
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " CRC32, the checksum specified is the same as the one in the policy"
                            + " and the checksum is not the same as the one generated by aws",
                        createDefaultPostParamBuilder().withChecksumCrc32("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-checksum-crc32", "wrongChecksum"),
                        false),
                // checksum-CRC32C
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " CRC32C, the checksum specified is not the same as the one in the"
                            + " policy and the checksum is the same as the one generated by aws",
                        createDefaultPostParamBuilder().withChecksumCrc32c("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32c", "fPxmpw=="),
                        false),
                // checksum-CRC32C
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is"
                            + " CRC32C, the checksum specified is not the same as the one in the"
                            + " policy and the checksum is not the same as the one generated by"
                            + " aws",
                        createDefaultPostParamBuilder().withChecksumCrc32c("wrongChecksum").build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-checksum-crc32c", "wrongChecksum"),
                        false),
                // x-amz-server-side-encryption
                of(
                        "Should fail while uploading file to S3 when the server-side-encryption"
                                + " specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption", "AES256"),
                        false),
                // server-side-encryption-aws-kms-key-id
                of(
                        "Should fail while uploading file to S3 when the"
                            + " server-side-encryption-aws-kms-key-id specified is not the same as"
                            + " the one in the policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionAwsKmsKeyId(
                                        System.getenv("AWS_KMS_S3_KEY"))
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption",
                                "aws:kms",
                                "x-amz-server-side-encryption-aws-kms-key-id",
                                "wrongKmsKeyValue"),
                        false),
                // server-side-encryption-context
                of(
                        "Should fail while uploading file to S3 when the base64 encoded json"
                            + " server-side-encryption-context specified is not the same as the one"
                            + " in the policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionContext("ewogICJ0ZXN0IjogInRlc3QiCn0=")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption",
                                "aws:kms",
                                "x-amz-server-side-encryption-context",
                                "abcdeICJ0ZXN0IjogInRlc3QiCn0="),
                        false),
                // server-side-encryption-context
                of(
                        "Should fail while uploading file to S3 when the base64 encoded json "
                                + "server-side-encryption-context specified is not a json",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionContext("dGhpcyBpcyBhIHRlc3Q=")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption",
                                "aws:kms",
                                "x-amz-server-side-encryption-context",
                                "dGhpcyBpcyBhIHRlc3Q="),
                        false),
                // server-side-encryption-bucket-key-enabled
                of(
                        "Should fail while uploading file to S3 when the"
                            + " server-side-encryption-bucket-key-enabled specified is not the same"
                            + " as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryption(
                                        PostParams.Builder.EncryptionAlgorithm.AWS_KMS)
                                .withServerSideEncryptionBucketKeyEnabled()
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption",
                                "aws:kms",
                                "x-amz-server-side-encryption-bucket-key-enabled",
                                "false"),
                        false),
                // x-amz-server-side-encryption-customer-algorithm
                // x-amz-server-side-encryption-customer-key
                // x-amz-server-side-encryption-customer-key-MD5
                of(
                        "Should fail while uploading file to S3 when customer-provided encryption"
                            + " key condition= specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryptionCustomerAlgorithmAES256()
                                .withServerSideEncryptionCustomerKey(
                                        encodeToBase64(encryptionKey256bits))
                                .withServerSideEncryptionCustomerKeyMD5(
                                        generateEncryptionKeyMD5DigestAsBase64())
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption-customer-algorithm", "AES256",
                                "x-amz-server-side-encryption-customer-key", "wrongBase64value",
                                "x-amz-server-side-encryption-customer-key-MD5",
                                        generateEncryptionKeyMD5DigestAsBase64()),
                        false),
                // x-amz-server-side-encryption-customer-algorithm
                // x-amz-server-side-encryption-customer-key
                // x-amz-server-side-encryption-customer-key-MD5
                of(
                        "Should succeed while uploading file to S3 when customer-provided"
                                + " encryption conditions specified are the same as the ones in the"
                                + " policy",
                        createDefaultPostParamBuilder()
                                .withServerSideEncryptionCustomerAlgorithmAES256()
                                .withServerSideEncryptionCustomerKey(
                                        encodeToBase64(encryptionKey256bits))
                                .withServerSideEncryptionCustomerKeyMD5(
                                        generateEncryptionKeyMD5DigestAsBase64())
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-server-side-encryption-customer-algorithm", "AES256",
                                "x-amz-server-side-encryption-customer-key",
                                        encodeToBase64(encryptionKey256bits),
                                "x-amz-server-side-encryption-customer-key-MD5",
                                        "wrongBase64EncryptionKeyMD5Digest"),
                        false));
    }

    private static byte[] generateFileChecksum(MessageDigest digest, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            return digest.digest();
        } catch (FileNotFoundException e) {
            LOGGER.error(
                    "Could not find file " + file.getAbsolutePath() + " to generate its digest", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error(
                    "Input/Output error while generating digest for file " + file.getAbsolutePath(),
                    e);
            throw new RuntimeException(e);
        }
    }

    private static String generateChecksumSha256Base64Encoded() {
        return generateChecksumBase64Encoded("SHA-256");
    }

    private static String generateChecksumSha1Base64Encoded() {
        return generateChecksumBase64Encoded("SHA-1");
    }

    private static String generateChecksumBase64Encoded(String algorithm) {
        try {
            File file = new File("src/test/resources/test.txt");
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] shaChecksum = generateFileChecksum(messageDigest, file);
            return Base64.getEncoder().encodeToString(shaChecksum);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not get instance of MessageDigest", e);
            throw new IllegalStateException(e);
        }
    }
}
