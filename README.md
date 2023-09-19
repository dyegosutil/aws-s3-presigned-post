# AWS S3 Pre Signed Post

Generates authenticated request data to be used by a chosen http client to upload files to AWS S3 using POST.

The library receives the mandatory and optional parameters alongside with the conditions supported by AWS S3 giving back the data
to be used for the upload, including policy and the signature generated using the
[AWS Signature Version 4](https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html) specification:


|     Param Name     | Value                                                              |
|:------------------:|:-------------------------------------------------------------------|
|       `key`        | `uploads/my_file.txt`                                              |
| `x-amz-algorithm`  | `AWS4-HMAC-SHA256`                                                 |
| `x-amz-credential` | `AKIA0000000000000000/20221009/eu-central-1/s3/aws4_request`       |
| `x-amz-signature`  | `a4cee4221d15970bf80e24b393658465fdf67e32df7bbd77e54f5182d3a14esd` |
|    `x-amz-date`    | `20231009T212242Z`                                                 |
|      `policy`      | `{"expiration":"2022-10-09T21:32:42.682Z" ...}`                    |

## Usage

[//]: # (TODO add warning to first create the bucket and configure it)
### 1. Add the dependency

[//]: # (TODO Correct version of library to automatic and correct name and version)
Gradle Kotlin:
```kotlin
implementation("io.github.dyegosutil:aws-s3-presigned-post:0.1.0-beta.3")
```

Maven:
```xml
<dependency>
    <groupId>io.github.dyegosutil</groupId>
    <artifactId>aws-s3-presigned-post</artifactId>
    <version>0.1.0-beta.3</version>
</dependency>
```

### 2. Create the `PreSignedPost`

First make sure that the AWS credentials are provided using any of 
[these possibilities](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) and that the access
key being used has the proper permission to upload in the bucket. 

After that, use the `PostParams` builder to specify the parameters and conditions for the upload:

```java
ZonedDateTime oneMinuteFromNow = now(systemUTC()).plus(1, MINUTES).atZone(UTC);

PostParams postParams = PostParams
    .builder(
        Region.of("eu-central-1"),      // AWS Region of the bucket
        oneMinuteFromNow,               // Expiration date 
        "myBucket",                     // Bucket name
        withKey("uploads/my_file.txt")  // Name of the S3 object key
    )
    .withContentLengthRange(7, 20)      // Adds file size upload limit
    .build();

// Generates all the necessary parameters for the pre-signed post including signature
PreSignedPost presignedPost = S3PostSigner.sign(postParams);
```

### 3. Upload the file

Use a http client in any platform to upload the file using the generated parameters from PreSignedPost.  
Below is a minimalistic example using [OkHttp](https://github.com/square/okhttp)

```java
MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
presignedPost.getConditions().forEach(builder::addFormDataPart);
builder.addFormDataPart(
        "file",
        "uploads/my_file.txt",
        RequestBody.create("This is a test".getBytes(), MediaType.parse("text/plain"))
);
Request request = new Request
        .Builder()
        .url(presignedPost.getUrl())
        .post(builder.build())
        .build();
try (Response response = new OkHttpClient().newCall(request).execute()) {
    if (!response.isSuccessful()) {
        throw new IOException("Upload failed. Unexpected code " + response);
    } else {
        LOGGER.info("Upload executed successfully");
    }
}
```
That is it. The file should be in S3 now.

## Additional code snippets:

Allow the uploader to use the name of the file being upload as S3 object Key:
```java
PostParams.builder(Region.EU_CENTRAL_1, EXPIRATION_DATE, "myBucket", withAnyKey());
// Additionally, when passing the last parameter `key` to the http client, use the value `${filename}`
```

Allow the uploader to upload to a path starting with a defined value and to use the name of the file being upload as S3
object Key:
```java
PostParams.builder(Region.EU_CENTRAL_1, EXPIRATION_DATE, "myBucket", "user/leo/box/");
// Additionally, when passing the last parameter `key` to the http client, use the value `user/leo/box/${filename}`
```

Only allow upload if the object key is `test.txt`(the http param, not the file name) and the file size is between
  7 and 20 bytes
```java
PostParams
        .builder(Region.EU_CENTRAL_1, EXPIRATION_DATE, "myBucket", withKey("test.txt"))
        .withContentLengthRange(7, 20)
        .build())
```

Allow the uploader to add any content type but enforce that the server side encryption `AES256` should be specified. 
```java
PostParams
        .builder(Region.EU_CENTRAL_1, EXPIRATION_DATE, "myBucket", withKey("test.txt"))
        .withAnyContentType()
        .withServerSideEncryption(AES256)
        .build())
```

For more examples look into the `integrationtests` package inside `src/test`.

## Features

- Provides a guided approach with a builder to create a non-error prone `PreSignedPost`
- Adding the following conditions using the util methods `withConditionName`, `withAnyConditionName` and  
`withConditionNameStartingWith`. The `ConditionName` here can be any of the following:
    - Bucket
    - Region
    - Expiration Date
    - Key Name
    - Tags
    - File size
    - Success Action Status
    - Cache Control
    - File Content Type
    - Content Disposition
    - Content encoding
    - File Expire
    - Success Action Redirect
    - Acl
    - Metadata
    - Storage Class
    - Website Redirect Location
    - Checksum CRC-32
    - Checksum CRC-32C
    - Checksum SHA-1
    - Checksum SHA-256
    - Server Side Encryption Algorithm
    - AWS KMS KEY to be used to for server-side encryption
    - Server Side Encryption Context
    - Allows specifying if Amazon S3 should use an S3 Bucket Key with SSE-KMS or not
    - Allows specifying the algorithm to use to when encrypting the object.
    - Allows specifying the base64 encoded encryption key to be used for this file upload
    - Allows specifying the base64 encoded 128-bit MD5 digest of the encryption key
- `PresignedFreeTextPost`: an advanced option that provides flexibility for creating a Pre Signed Post on which
  parameters and conditions can be provided freely. Even if this library does not support a new AWS feature, using this
  approach will probably make it possible to use it. Note that this option requires some understanding of the AWS request
  creation and signing process.

## Notes
- Java 8 compatible
- Remember that when using `withConditionNameStartingWith`, the library cannot foresee which value should be used in the client. Hence, 
the PreSignedPost will provide only the `key` but not the `value` for this data letting the uploader decide how to fill up this value.
- The library uses `DefaultCredentialsProvider` to obtain the aws credentials.
  Check [this](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) page to decide how to provide it
- If an `ASIA` aws access key is found, the library will return a new param `x-amz-security-token`.
- To allow the user to upload any key name, use ```withAnyKey()``` and submit in the http call for the `key` param
  the value `${filename}`. Note that `${filename}` variable is not compatible with `withKey()` condition.
  Only for `withKeyStartingWith()` and `withAnyKey()`.
- When ```content-length-range``` is used, it is not necessary to specify this condition while using the pre signed post,
  even though it is in the policy. Note that this is the only exception, all other valuedConditions should be passed to aws
  otherwise it will return an error
- Generating S3 post data for uploading files into public access s3 buckets is not included in this library since it is pretty straight forward.
  That is, the only parameters necessary are the ```key``` and ```file```.

## Logging

<details>
<summary><i>Click to expand</i></summary>

Nothing passed as parameter to the library is logged in any level to avoid logging possible PII data.  
If debug log level is enabled, the only data logged is the:
- Current now date used to build certain request params.
- The enum name of conditions used to build the Pre-signed post such as: `BUCKET, SUCCESS_ACTION_REDIRECT, KEY`

If there is the need to log more data, it can be done by decoding the base64 policy param returned in the `PresignedPost`
</details>

## Running locally
<details>
<summary><i>Click to expand</i></summary>
To run normal unity tests no additional configuration is needed.
To run the integration tests, the following environment variables are necessary according to the aws account being
used:

```
AWS_ACCESS_KEY_ID=AKIAEXAMPLE
AWS_BUCKET=mybucket
AWS_REGION=eu-central-1
AWS_SECRET_ACCESS_KEY=mysecretaccesskey
AWS_KMS_S3_KEY=arn:aws:kms:my_region:my_account_id:key/my-key
```

To run the integration tests that use ASIA credentials, the additional environment variable `AWS_SESSION_TOKEN` is necessary.
Below is one way of getting the credentials. [AWS cli](https://aws.amazon.com/cli/) and [aws-vault](https://github.com/99designs/aws-vault) is a requirement

For Mac OS:
```shell
aws-vault exec my_profile -- env | egrep '^AWS_(ACCESS_KEY_ID|SECRET_ACCESS_KEY|SESSION_TOKEN)'
```
For Windows:
```shell
aws-vault exec my_profile 
ECHO "AccessKeyId": "%AWS_ACCESS_KEY_ID%", "SecretAccessKey": "%AWS_SECRET_ACCESS_KEY%", "SessionToken": "%AWS_SESSION_TOKEN%"
```
</details>

## Troubleshooting

<details>
<summary><i>Click to expand</i></summary>

### Cannot restrict key value

#### Description

The `PostParam` was created using , for example, `withKey("uploads/my_file.txt")` and all the conditions from `PresignedPost`
were added to the http client post request. But when the http client `file` condition is changed to `uploads/wrong_file_name.txt` or
when the name of the uploaded file is `wrong_file_name.txt`, the upload still works when it should have failed.

#### Solution

The parameter `key` set in the request has the correct value `uploads/my_file.txt`.  
When `withKey("uploads/my_file.txt")` is used, the value of the parameter `file` or the name of the file being uploaded
is not considered by AWS. What matter is that the value of the parameter `key`. That is how the file will be named in S3.    
Note that the file name would matter if `startWith()` and `${filename}` would have been used in `PostParam` and in the http
client request respectively.

### If while trying to use ACLs you receive the message

#### Description

While trying to use ACLs you receive the message

```
<Message>The bucket does not allow ACLs</Message>
```

#### Solution

First have to enable ACL usage in the bucket before using it in the pre-signed post.
</details>

## Goals

- Encapsulate complexity of validations as well as compatibility of conditions, policy and signature generation
- Preventing the generation of faulty pre signed post data
- Avoiding the library user having to dive into the AWS documentation do understand how to use the conditions.
- Provide friendly intuitive methods for specifying conditions and for generating the Pre Signed Post.

## Motivation

The AWS Java SDK 2 does not support the generation of Pre Signed Post, different from other AWS SDKs such as JS which does
support it. What is offered for Java is the [Pre Signed Put](https://github.com/aws/aws-sdk-java-v2/blob/13887532e50932bb3a93680884e94bd087a58abb/services/s3/src/main/java/software/amazon/awssdk/services/s3/presigner/S3Presigner.java#L325) which does not support all the conditions available in the Pre Signed Post such as limiting the file size of the upload
and many others.

## Contributing

Feel free to engage, report bugs or give feedback by creating an Issue.

## Reference documents

- [AWS S3 Post Policy](https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html)
- [AWS S3 Post Object](https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html)
- [Signing AWS API requests](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_aws-signing.html)