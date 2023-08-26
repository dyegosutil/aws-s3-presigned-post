# AWS S3 Pre Signed Post

Generates authenticated request data to be used later by a http client to upload files to AWS S3 using POST.

The library receives the mandatory and optional parameters alongside with the conditions supported by AWS S3 giving back the data 
to be used for the upload, including policy and the signature generated using the
[AWS Signature Version 4](https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html) specification


|    Param Name    |                              Value                               |
|:----------------:|:----------------------------------------------------------------:|
|       key        |                       uploads/my_file.txt                        |
| x-amz-algorithm  |                         AWS4-HMAC-SHA256                         |
| x-amz-credential |    AKIA0000000000000000/20221009/eu-central-1/s3/aws4_request    |
| x-amz-signature  | a4cee4221d15970bf80e24b393658465fdf67e32df7bbd77e54f5182d3a14esd |
|    x-amz-date    |                         20221009T212242Z                         |
|      policy      |       ```{"expiration":"2022-10-09T21:32:42.682Z" ...} ```       |

## Motivation

The reason for creating this library is that AWS Java SDK 2 does not support the generation of Pre Signed Post. 
Different from other AWS SDKs such as JS which does support it. 

What is offered in  AWS Java SDK 2 is the 
[Pre Signed Put](https://github.com/aws/aws-sdk-java-v2/blob/13887532e50932bb3a93680884e94bd087a58abb/services/s3/src/main/java/software/amazon/awssdk/services/s3/presigner/S3Presigner.java#L325)
which does not support all the conditions available in the Pre Signed Post such as limiting the file size of the upload
and many others.

## How to use it

[//]: # (TODO add warning to first create the bucket and configure it)
### 1. Add the dependency

[//]: # (TODO Correct version of library to automatic and correct name and version)
Gradle Kotlin:
```kotlin
implementation("mendes.sutil.dyego:aws-s3-presigned-post:1.0-SNAPSHOT")
```

### 2. Create the `PreSignedPost`

Use the Builder to specify the parameters and conditions for the upload:

```java
ZonedDateTime oneMinuteFromNow = now(systemUTC()).plus(1, MINUTES).atZone(UTC);

PostParams postParams = PostParams
    .builder(
        Region.of("eu-central-1"),      // AWS Region of the bucket
        oneMinuteFromNow,               // Expiration date 
        "dyegosutil",                   // Bucket name
        withKey("uploads/my_file.txt")  // Name of the S3 object key
    )
    .withContentLengthRange(7, 20)      // Adds file size upload limit
    .build();

// Generates all the necessary parameters for the pre-signed post including signature
PreSignedPost presignedPost = S3PostSigner.sign(postParams, getAmazonCredentialsProvider());
```

Here is one example of how to create a `AwsCredentialsProvider` for tests

```java
public AwsCredentialsProvider getAmazonCredentialsProvider() {
    return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(System.getenv("AWS_KEY"), System.getenv("AWS_SECRET"))
        );
}
```

### 3. Upload the file

Use a http client in any platform to upload the file using the generated parameters from PreSignedPost.  
Below is a minimalistic example using OkHttp

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
## Features

- `PreSignedPost` creation: Provides a guided approach with a builder to create a non-error prone `PreSignedPost`
- `PresignedFreeTextPost` creation: Advanced option that provides flexibility for creating a Pre Signed Post on which
parameters and conditions can be provided freely. Even if this library does not support a new AWS feature, using this
approach will probably make it possible to use it. Note that this option requires some understanding of the AWS request
creation and signing process.
- Adding conditions/restrictions:  
Example: You need to create a Pre Signed Post that will allow the user to upload a file with the name starting with 
"Full_Report_", not bigger than 100 MB and having the tag confidential=true.  
Note that for each condition util methods `withConditionName` and/or 
`withConditionNameStartingWith` are available. Below are all the supported conditions. For full details of the conditions
check the class `PostParams`:
  - Bucket
  - Region
  - Expiration Date, that is, date until the pre signed post expire
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

## How to

- Use the name of the file being upload as S3 object Key
```java

```
- Allow any S3 object key name
```java

```
- Allow the upload only of requests on which the S3 object key name start with xxxxxxx
```java

```


## Notes
- If you want to allow the user upload any key use ```withAnyKey()``` and submit as key name ```${filename}```
- Generating S3 post data for uploading files into public access s3 buckets is not included in this library since it is pretty straight forward.
That is, the only parameters necessary are the ```key``` and ```file```.

- When ```content-length-range``` is used, it is not necessary to specify this condition while using the pre signed post,
even though it is in the policy. Note that this is the only exception, all other valuedConditions should be passed to aws 
otherwise it will return an error

- The {filename} variable does not work for eq. Only for startsWith.
The reason is that in the policy we cannot simply specify "". It has to have a value. Otherwise, the signature check will fail. Amazon will check in the end name_of_user_file == "" And the signature will fail. Hence the value has to be passed by the one calling the lib.
For the startWith, it is okay since you will specify at list on character in front of the user file name.

# Features to be added
To accept any value for a certain condition, use the ```with*StartingWith``` passing an empty string ```""``` such as ```withAclStartingWith("")```, ```withContentEncodingStartingWith("")```, etc
In next releases, the method ```withAny*``` will be made available such as  ```withAnyAcl```, ```withAnyContentEncoding```, etc

## Running locally

The Integration Tests use AWS_KMS_S3_KEY to test the server-serid encryption.
The best way to configure it is to run the IT xyz which will create a encription key
it is does exist yet for s3. Go to KMS in AWS and copy the key from there. It should
be in the following format: "arn:aws:kms:region:acct-id:key/key-id"

To run the integration tests that uploads files so S3, the following environment variables need to be set:

```
AWS_KEY = Your AWS key such as AKIA...
AWS_SECRET = your secret
AWS_BUCKET = The bucket name where the test files must be uploaded to. Ex: 'testbucket'
AWS_REGION = Ex: 'eu-central-1' any regition that can be used with Region.of(). This should be the region for the valid bucket your have configured to test the uploads.
AWS_SESSION_TOKEN ?  
```


Expalin that this is the return when you set 201 as response

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PostResponse>
    <Location>https://dyegosutil.s3.eu-central-1.amazonaws.com/pira.txt</Location>
    <Bucket>dyegosutil</Bucket>
    <Key>pira.txt</Key>
    <ETag>"d41d8cd98f00b204e9800998ecf8427e"</ETag>
</PostResponse>
```

### How to get a session token

```shell
aws-vault exec default --duration=12h -- env | egrep '^AWS_(ACCESS_KEY_ID|SECRET_ACCESS_KEY|SESSION_TOKEN)'
```

Important notes:
- Even if you are not adding a withSessionToken, if the credentials are temporary, a condition ```x-amz-security-token``` will be adde dot the policy and you will have to add it to the request.

Interesting resource
http://s3.amazonaws.com/doc/s3-example-code/post/post_sample.html

If while trying to use ACLs you receive the message 
```
<Message>The bucket does not allow ACLs</Message>
```
it is because you first have to enable ACL usage in the bucket before using it int he pre-signed post.

## Troubleshooting

### Cannot restrict key value

#### Description

The `PostParam` was created using , for example, `withKey("uploads/my_file.txt")` and all the conditions from `PresignedPost`
were added to the http client post request. But when the http client `file` condition is changed to `uploads/wrong_file_name.txt` or 
when the name of the uploaded file is `wrong_file_name.txt`, the upload still works when it should have failed.

#### Solution
 
The parameter `key` set in the request has the correct value `uploads/my_file.txt`.  
When `withKey("uploads/my_file.txt")` is used, the value of the parameter `file` or the name of the file being uploaded 
is not considered by AWS.What matter is that the value of the parameter `key` and that is how the file will be named in S3.    
Note that the file name would matter if `startWith()` and `{filename}` would have been used in `PostParam` and in the http 
client request respectively.

## Goals

- Encapsulate complexity of validations as well as compatibility of conditions, policy and signature generation.
- Prevent generation of faulty Pre Signed Post data
- Save time preventing the user having to dive into the AWS documentation do understand how to use the feature.
- Provide friendly intuitive methods for specifying conditions and for generating the Pre Signed Post.

# To be done
- to be done: inform how to activate logging
- Add examples of how to use each one of the options, content type, range, etc.
- Check where info should not be null?
- does not provide support for amaazon devpay
- explain that if it is being used temporary credentials, it will be added automatically.
- Add tags validations:
  https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-tagging.html
- Check if all acccess modifiers are all right, private, protected, etc.
- Please Report any limitations that you found so that I can add validations.
- Think about removing jaxb
- Check about license, and legal matters=
- Check what does it mean to develop open source, what to expect, what is good,
- what is bad, what is expected from me, what is not expected.
- List capabilities of the lib/pre-signed post
- try to remove dependencies, amazon, etc
- search about what to think when doing a lib, how should be the methods for the person using it,
- the construcutor, the builder, and any other things to take in consideration  code wise.
- warns gradle build or java build
- See if I can watch the pages of doc from amazon in case they change something.
- check if I should remove UTC and go for the default system time zone
- Double check the AWS_SESSION_TOKEN, AWS_SESSION_SECRET and AWS_SESSION_TOKEN, one or more values might be wrong
- Make sure you are not mixing AWS_KEY and AWS_SECRET with session credentials
- ASIAUNVUIU7WKO5WTRO2
- creating non offical libraries, what to take care, can aws sue me, naming, should I say it is non official, etc
- Add warn when date is too long
- try to remove aws dependencies?
- HAVE A look in well know libraries souce code to see what they are using
- Should I think about put as well? or naming the library with a more generic name?
- Add final where it should be
- Test logging in ECS.
- Add id to pre signed post generation to get meaningfull loggings, userId, etc
- Have one more look in the logs, Input validation failures e.g. protocol violations, unacceptable encodings, invalid parameter names and values
- how to make your library to be found by searches on google.
- Regenerate any access key, password, token, just to make sure.

Trivial 

- Use credentials defined in my computer instead of env variables
- Explain point above in the read.me

## Contributing

## Reference documents

- Post Policy - https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html
- https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html

- Environment Variables necessary to run all integration tests
```
AWS_SESSION_TOKEN=value;AWS_REGION=eu-central-1;AWS_KEY=value;AWS_SECRET=value;AWS_KMS_S3_KEY=arn:aws:kms:eu-central-1:xxxxxxxxxxxx:key/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx;AWS_SESSION_SECRET=value;AWS_SESSION_KEY=ASIA...;AWS_BUCKET=myBucket
```
Add info from this page
https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html

## Logging

Nothing passed as parameter to the library is logged in any level to avoid logging possible PII data.  
If debug log level is enabled, the only data logged is the:
- Current now data used to build the `x-amz-credential` value
- And the enum name of conditions used to build the Pre-signed post such as: `BUCKET,SUCCESS_ACTION_REDIRECT,KEY`

If there is the need to log more data, it can be done by decoding the base64 policy param returned by the library