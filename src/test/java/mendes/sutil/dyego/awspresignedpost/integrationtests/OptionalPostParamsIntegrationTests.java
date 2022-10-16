package mendes.sutil.dyego.awspresignedpost.integrationtests;

import mendes.sutil.dyego.awspresignedpost.PostParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static mendes.sutil.dyego.awspresignedpost.PostParams.Builder.CannedAcl.PRIVATE;
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
        return Stream.of(
                // content-length-range
                of("Should succeed while uploading file to S3 when it's size is between the minimum and maximum specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(7, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of("Should succeed while uploading file to S3 when it's size is of the exact size specified values in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(14, 14)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        true
                ),
                // content-length-range
                of("Should fail while uploading file to S3 when it's size is over the maximum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(1, 2)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // content-length-range
                of("Should fail while uploading file to S3 when it's size is under the minimum specified value in the policy",
                        createDefaultPostParamBuilder()
                                .withContentLengthRange(15, 20)
                                .build(),
                        createFormDataParts("key", "${filename}"),
                        false
                ),
                // Cache-Control
                of("Should succeed while uploading file to S3 when the cache-control specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of("Should fail while uploading file to S3 when the cache-control specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                ),
                // Cache-Control
                of("Should succeed while uploading file to S3 when the cache-control specified starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControlStartingWith("public,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7200"),
                        true
                ),
                // Cache-Control
                of("Should fail while uploading file to S3 when the cache-control specified does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withCacheControl("public, max-age=7200")
                                .build(),
                        createFormDataPartsWithKeyCondition("Cache-Control", "public, max-age=7201"),
                        false
                ),
                // Content-Type
                of("Should succeed while uploading file to S3 when the exact Content-Type specified is the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withContentType("text/plain")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        true
                ),
                // Content-Type
                of("Should fail while uploading file to S3 when the exact Content-Type specified is not the same as the one in the policy",
                        createDefaultPostParamBuilder()
                                .withContentType("text/plain")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "Aext/plain"),
                        false
                ),
                // Content-Type
                of("Should succeed while uploading file to S3 when the Content-Type specified starts with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withContentTypeStartingWith("tex")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        true
                ),
                // Content-Type
                of("Should fail while uploading file to S3 when the Content-Type specified does not start with the same value specified in the policy",
                        createDefaultPostParamBuilder()
                                .withContentTypeStartingWith("dex")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Type", "text/plain"),
                        false
                ),
                // Content Disposition
                of("Should succeed while uploading file to S3 using the exact content disposition set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDisposition("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "inline"),
                        true
                ),
                // Content Disposition
                of("Should fail while uploading file to S3 not using the exact content disposition set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDisposition("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false
                ),
                // Content Disposition
                of("Should succeed while uploading file to S3 using the content disposition starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inli")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "inline"),
                        true
                ),
                // Content Disposition
                of("Should fail while uploading file to S3 using the content disposition starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentDispositionStartingWith("inline")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "attachment"),
                        false
                ),
                // Content-Encoding
                of("Should succeed while uploading file to S3 using the exact content encoding set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncoding("compress")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "compress"),
                        true
                )
                ,
                // Content-Encoding
                of("Should fail while uploading file to S3 not using the exact content encoding set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncoding("compress")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "gzip"),
                        false
                ),
                // Content-Encoding
                of("Should succeed while uploading file to S3 using the content encoding starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Encoding", "compress"),
                        true
                ),
                // Content-Encoding
                of("Should fail while uploading file to S3 using the content encoding starting with value different than the one set in the policy",
                        createDefaultPostParamBuilder()
                                .withContentEncodingStartingWith("com")
                                .build(),
                        createFormDataPartsWithKeyCondition("Content-Disposition", "abc"),
                        false
                ),
                // Expires
                of("Should succeed while uploading file to S3 using the exact Expires condition set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:28:00 GMT"), // TODO use Expires as a constant? So that it can be seen that this is how it should be passed in the browser params or postman?
                        true
                )
                ,
                // Expires
                of("Should fail while uploading file to S3 not using the exact Expires condition set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpires("Wed, 21 Oct 2015 07:28:00 GMT")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        false
                ),
                // Expires
                of("Should succeed while uploading file to S3 using the Expires condition starting with value as set in the policy",
                        createDefaultPostParamBuilder()
                                .withExpiresStartingWith("Wed,")
                                .build(),
                        createFormDataPartsWithKeyCondition("Expires", "Wed, 21 Oct 2015 07:29:00 GMT"),
                        true
                ),
                // Expires
                of("Should fail while uploading file to S3 using the Expires starting with value different than the one set in the policy",
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
                )
        );
    }
}
