package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.PostParams.Builder.CannedAcl.PRIVATE;
import static mendes.sutil.dyego.awspresignedpost.PostParams.Builder.StorageClass.STANDARD;
import static org.junit.jupiter.params.provider.Arguments.of;

@Disabled
public class OptionalPostParamsIntegrationTests extends IntegrationTests {

    /**
     * Generates the pre-signed post using the mandatory params and also optional params performing the upload to S3.
     *
     * @param testDescription
     * @param postParams
     * @param formDataParts
     * @param expectedResult
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCasesOptionalParams")
    void testWithOptionalParams(
            String testDescription,
            PostParams postParams,
            Map<String, String> formDataParts,
            Boolean expectedResult
    ) {
        createPreSignedPostAndUpload(postParams, formDataParts, expectedResult);
    }

    private static Stream<Arguments> getTestCasesOptionalParams() {
        String tagging = "<Tagging><TagSet><Tag><Key>MyTestTag</Key><Value>MyTagValue</Value></Tag></TagSet></Tagging>";
        return Stream.of(
                // content-length-range
                of(
                        "Should succeed while uploading file to S3 when it's size is between the minimum and maximum specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(7, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of(
                        "Should succeed while uploading file to S3 when it's size is of the exact size specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(14, 14)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of(
                        "Should fail while uploading file to S3 when it's size is over the maximum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(1, 2)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // content-length-range
                of(
                        "Should fail while uploading file to S3 when it's size is under the minimum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(15, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // Cache-Control
                of(
                        "Should succeed while uploading file to S3 when the cache-control specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of(
                        "Should fail while uploading file to S3 when the cache-control specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                ),
                // Cache-Control
                of(
                        "Should succeed while uploading file to S3 when the cache-control specified starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControlStartingWith("public,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of(
                        "Should fail while uploading file to S3 when the cache-control specified does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                ),
                // Content-Type
                of(
                        "Should succeed while uploading file to S3 when the exact Content-Type specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withContentType("text/plain")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        true
                ),
                // Content-Type
                of(
                        "Should fail while uploading file to S3 when the exact Content-Type specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withContentType("text/plain")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "Aext/plain"),
                        false
                ),
                // Content-Type
                of(
                        "Should succeed while uploading file to S3 when the Content-Type specified starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withContentTypeStartingWith("tex")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        true
                ),
                // Content-Type
                of(
                        "Should fail while uploading file to S3 when the Content-Type specified does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withContentTypeStartingWith("dex")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        false
                ),
                // Content Disposition
                of(
                        "Should succeed while uploading file to S3 using the exact content disposition set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDisposition("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "inline"),
                        true
                ),
                // Content Disposition
                of(
                        "Should fail while uploading file to S3 not using the exact content disposition set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDisposition("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false
                ),
                // Content Disposition
                of(
                        "Should succeed while uploading file to S3 using the content disposition starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inli")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "inline"),
                        true
                ),
                // Content Disposition
                of(
                        "Should fail while uploading file to S3 using the content disposition starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false
                ),
                // Content-Encoding
                of(
                        "Should succeed while uploading file to S3 using the exact content encoding set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncoding("compress")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "compress"),
                        true
                )
                ,
                // Content-Encoding
                of(
                        "Should fail while uploading file to S3 not using the exact content encoding set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncoding("compress")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "gzip"),
                        false
                ),
                // Content-Encoding
                of(
                        "Should succeed while uploading file to S3 using the content encoding starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "compress"),
                        true
                ),
                // Content-Encoding
                of(
                        "Should fail while uploading file to S3 using the content encoding starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "abc"),
                        false
                ),
                // Expires
                of(
                        "Should succeed while uploading file to S3 using the exact Expires condition set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:28:00 GMT"), // TODO use Expires as a constant? So that it can be seen that this is how it should be passed in the browser params or postman?
                        true
                )
                ,
                // Expires
                of(
                        "Should fail while uploading file to S3 not using the exact Expires condition set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        false
                ),
                // Expires
                of(
                        "Should succeed while uploading file to S3 using the Expires condition starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpiresStartingWith("Wed,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        true
                ),
                // Expires
                of(
                        "Should fail while uploading file to S3 using the Expires starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpiresStartingWith("Wed,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Mon, 21 Oct 2015 07:29:00 GMT"),
                        false
                ),
                // acl
                of(
                        "Should succeed while uploading file to S3 when the acl specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withAcl(PRIVATE)
                                .build(),
                        createFormDataPartsWithKeyCondition("acl", "private"),
                        true
                ),
                // acl
                of(
                        "Should fail while uploading file to S3 when the acl specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withAcl(PRIVATE)
                                .build(),
                        createFormDataPartsWithKeyCondition("acl", "wrongValue"),
                        false
                ),
                // acl
                of(
                        "Should succeed while uploading file to S3 when the acl starts with value specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withAclStartingWith("pri")
                                .build(),
                        createFormDataPartsWithKeyCondition("acl", "private"),
                        true
                ),
                of(
                        "Should fail while uploading file to S3 when the acl starts with value specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withAclStartingWith("abc")
                                .build(),
                        createFormDataPartsWithKeyCondition("acl", "private"),
                        false
                ),
                // tagging
                of(
                        "Should succeed while uploading file to S3 when it's free text tagging value is the same as the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTagging(tagging)
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", tagging),
                        true
                ),
                // tagging
                of(
                        "Should fail while uploading file to S3 when it's free text tagging is the same as the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTagging(tagging)
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", "wrongValue"),
                        false
                ),
                // tagging
                of(
                        "Should succeed while uploading file to S3 when it's tagging is the same as the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTag("myTagKey", "myTagValue")
                                .withTag("myTagKey2", "myTagValue2")
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", "<Tagging><TagSet><Tag><Key>myTagKey</Key><Value>myTagValue</Value></Tag><Tag><Key>myTagKey2</Key><Value>myTagValue2</Value></Tag></TagSet></Tagging>"),
                        true
                ),
                // tagging
                of(
                        "Should fail while uploading file to S3 when it's tagging is the same as the one specified in the policy",
                        createDefaultPostParamBuilder()
                                .withTag("myTagKey", "myTagValue")
                                .withTag("myTagKey2", "myTagValue2")
                                .build(),
                        createFormDataPartsWithKeyCondition("tagging", "wrongValue"),
                        false
                ),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the 1 meta specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data",
                                "value for my meta-data"
                        ),
                        true
                ),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the 2 metas specified are the same as the ones in the policy",
                        createDefaultPostParamBuilder()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .withMeta("my_meta_data2", "value for my meta-data2")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data",
                                "value for my meta-data",
                                "x-amz-meta-my_meta_data2",
                                "value for my meta-data2"
                        ),
                        true
                ),
                // meta
                of(
                        "Should fail while uploading file to S3 when the meta specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMeta("my_meta_data", "value for my meta-data")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data",
                                "not my meta"
                        ),
                        false
                ),
                // meta
                of(
                        "Should succeed while uploading file to S3 when the meta starting value is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMetaStartingWith("my_meta_data", "abcde")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data",
                                "abcdefg"
                        ),
                        true
                ),
                // meta
                of(
                        "Should fail while uploading file to S3 when the meta starting value is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withMetaStartingWith("my_meta_data", "abcde")
                                .build(),
                        createFormDataPartsWithKeyCondition(
                                "x-amz-meta-my_meta_data",
                                "xyz"
                        ),
                        false
                ),
                // storage-class
                of(
                        "Should succeed while uploading file to S3 when the storage class specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withStorageClass(STANDARD)
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-storage-class", "STANDARD"),
                        true
                ),
                // storage-class
                of(
                        "Should fail while uploading file to S3 when the storage class specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withStorageClass(STANDARD)
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-storage-class", "INTELLIGENT_TIERING"),
                        false
                ),
                // website-redirect-location
                of(
                        "Should succeed while uploading file to S3 when the website-redirect-location specified is the same as the one in the policy which redirects to a file in the same bucket",
                        createDefaultPostParamBuilder()
                                .withWebsiteRedirectLocation("/anotherPage.html")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-website-redirect-location", "/anotherPage.html"),
                        true
                ),
                // website-redirect-location
                of(
                        "Should succeed while uploading file to S3 when the website-redirect-location specified is the same as the one in the policy which redirects to another website",
                        createDefaultPostParamBuilder()
                                .withWebsiteRedirectLocation("https://www.google.com")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-website-redirect-location", "https://www.google.com"),
                        true
                ),
                // website-redirect-location
                of(
                        "Should fail while uploading file to S3 when the website-redirect-location specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withWebsiteRedirectLocation("/anotherPage.html")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-website-redirect-location", "/yetAnotherPage.html"),
                        false
                ),
                // checksum-sha256
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is SHA256, " +
                                "the checksum specified is the same as the one in the policy and the checksum is the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha256(generateChecksumSha256Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha256", generateChecksumSha256Base64Encoded()),
                        true
                ),
                // checksum-sha256
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is SHA256, " +
                                "the checksum specified is not the same as the one in the policy and the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha256(generateChecksumSha256Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha256", "wrongChecksum"),
                        false
                ),
                // checksum-sha256
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is SHA256, " +
                                "the checksum specified is the same as the one in the policy but the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha256("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha256", "wrongChecksum"),
                        false
                ),
                // checksum-sha1
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is SHA1, " +
                                "the checksum specified is the same as the one in the policy and the checksum is the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha1(generateChecksumSha1Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha1", generateChecksumSha1Base64Encoded()),
                        true
                ),
                // checksum-sha1
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is SHA1, " +
                                "the checksum specified is not the same as the one in the policy and the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha1(generateChecksumSha1Base64Encoded())
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha1", "wrongChecksum"),
                        false
                ),
                // checksum-sha1
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is SHA1, " +
                                "the checksum specified is the same as the one in the policy but the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumSha1("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-sha1", "wrongChecksum"),
                        false
                ),
                // checksum-CRC32
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is CRC32, " +
                                "the checksum specified is the same as the one in the policy and the checksum is the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32("DR7n6g==")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32", "DR7n6g=="),
                        true
                ),
                // checksum-CRC32
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is CRC32, " +
                                "the checksum specified is not the same as the one in the policy and the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32", "DR7n6g=="),
                        false
                ),
                // checksum-CRC32
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is CRC32, " +
                                "the checksum specified is the same as the one in the policy and the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32", "wrongChecksum"),
                        false
                ),
                // checksum-CRC32C
                of(
                        "Should succeed while uploading file to S3 when the checksum algorithm is CRC32C, " +
                                "the checksum specified is the same as the one in the policy and the checksum is the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32c("fPxmpw==")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32c", "fPxmpw=="),
                        true
                ),
                // checksum-CRC32C
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is CRC32C, " +
                                "the checksum specified is not the same as the one in the policy and the checksum is the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32c("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32c", "fPxmpw=="),
                        false
                ),
                // checksum-CRC32C
                of(
                        "Should fail while uploading file to S3 when the checksum algorithm is CRC32C, " +
                                "the checksum specified is not the same as the one in the policy and the checksum is not the" +
                                " same as the one generated by aws",
                        createDefaultPostParamBuilder()
                                .withChecksumCrc32c("wrongChecksum")
                                .build(),
                        createFormDataPartsWithKeyCondition("x-amz-checksum-crc32c", "wrongChecksum"),
                        false
                )
        );
    }

    private static byte[] generateFileChecksum(MessageDigest digest, File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();
        return digest.digest();
    }

    private static String generateChecksumSha256Base64Encoded() {
        return generateChecksumBase64Encoded("SHA-256");
    }

    private static String generateChecksumSha1Base64Encoded() {
        return generateChecksumBase64Encoded("SHA-1");
    }

    private static String generateChecksumBase64Encoded(String algorithm) {
        try {
            File file = new File("src/test/resources/test.txt");   //TODO shold be configuration?
            MessageDigest shaDigest = MessageDigest.getInstance(algorithm);
            byte[] shaChecksum = generateFileChecksum(shaDigest, file);
            return Base64.getEncoder().encodeToString(shaChecksum);
        } catch (NoSuchAlgorithmException | IOException e) {
            // TODO add log.error
            throw new RuntimeException(e);
        }
    }
}
