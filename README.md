![Build](https://github.com/dyegosutil/aws-s3-presigned-post/actions/workflows/github-actions-demo.yml/badge.svg)
# Aws S3 Presigned Post

Generating S3 post data for uploading files into public access s3 buckets is not included in this library since it is pretty straight forward. 
That is, the only parameters necessary are the ```key``` and ```file```.

# To be done
- double check if more debug log is needed

# How to use

## Notes

When ```content-length-range``` is used, it is not necessary to specify this condition while using the pre signed post,
even though it is in the policy. Note that this is the only exception, all other valuedConditions should be passed to aws 
otherwise it will return an error

# Features to be added

- Add a way to pass string params and make the lib compatible with any new fields aws might start supporting

# Reference documents

- Post Policy - https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html
- https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html

- Environment Variables necessary to run all integration tests
```
AWS_SESSION_TOKEN=value;AWS_REGION=eu-central-1;AWS_KEY=value;AWS_SECRET=value;AWS_KMS_S3_KEY=arn:aws:kms:eu-central-1:xxxxxxxxxxxx:key/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx;AWS_SESSION_SECRET=value;AWS_SESSION_KEY=ASIA...;AWS_BUCKET=muBucket;AWS_WRONG_REGION=eu-central-2
```

# Issues

sometimes seems that due to the ```=``` in the end of the policy, there was a signature problem. Removing one of the 3 = symbols in the end of the request made it work. Investigate this better. Perhaps print the policy withouth any = in the end to avoid problems. But test this first.
- include support for/check ```x-ignore-```
- Test to be done
  - upload with key in policy 
  - upload with key outside policy
- Check https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html to make everything in an url 
- Add formatting standards according to what is accepted by the community
- scan for any vulnerabilities in dependencies or code betterment.
- Use Value Objects


to be done: Add adapted desc of this text:
The following table describes a list of fields that you can use within a form.
Among other fields, there is a signature field that you can use to authenticate requests.
There are fields for you to specify the signature calculation algorithm (x-amz-algorithm),
the credential scope (x-amz-credential) that you used to generate the signing key, 
and the date (x-amz-date) used to calculate the signature. 
Amazon S3 uses this information to re-create the signature. 
If the signatures match, Amazon S3 processes the request.


to be done :Also important to add:
All this is for authenticated requests



The {filename} variable does not work for eq. Only for startsWith.
The reason is that in the policy we cannot simply specify "". It has to have a value. Otherwise the signature check will fail. Amazon will check in the end name_of_user_file == "" And the signature will fail. Hence the value has to be passed by the one calling the lib.
For the startWith, it is okay since you will specify at list on character in front of the user file name.


Add info from this page
https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html

Add list of features
- Helps you to build the pre-signed post with the minimum necessary params so that it will work
- You dont have to build the policies yourself, the builder simplify your work.

## Running locally

The IT tests use AWS_KMS_S3_KEY to test the server-serid encryption.
The best way to configure it is to run the IT xyz which will create a encription key
it is does exist yet for s3. Go to KMS in AWS and copy the key from there. It should
be in the following format: "arn:aws:kms:region:acct-id:key/key-id"

To run the integration tests that uploads files so S3, the following environment variables need to be set:

```
AWS_KEY = Your AWS key such as AKIA...
AWS_SECRET = your secret
AWS_BUCKET = The bucket name where the test files must be uploaded to. Ex: 'testbucket'
AWS_REGION = Ex: 'eu-central-1' any regition that can be used with Region.of(). This should be the region for the valid bucket your have configured to test the uploads.
AWS_WRONG_REGION = This should be a bucket which is not the one you have configured the bucket for. Any value that can be used with Region.of()
AWS_SESSION_TOKEN ?  
```
 

Also is necessary to remove the @Disabled annotation from the test zzz

to be done: inform how to activate logging

If you want to allow the user upload any key use ```withAnyKey()``` and submit as key name ```${filename}```


Add examples of how to use each one of the options, content type, range, etc.

Check where info should not be null?

how to make your library to be found by searches on google.


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

does not provide support for amaazon devpay

explain that if it is being used temporary credentials, it will be added automatically.

## How to get a session token
aws-vault exec default --duration=12h -- env | egrep '^AWS_(ACCESS_KEY_ID|SECRET_ACCESS_KEY|SESSION_TOKEN)'

Important notes:
- Even if you you are not adding a withSessionToken, if the credentials are temporary, a condition ```x-amz-security-token``` will be adde dot the policy and you will have to add it to the request.

Interesting resource
http://s3.amazonaws.com/doc/s3-example-code/post/post_sample.html

If while trying to use ACLs you receive the message 
```
<Message>The bucket does not allow ACLs</Message>
```
it is because you first have to enable ACL usage in the bucket before using it int he pre-signed post.

HEADERs not mapped
AWSAccessKeyId

Add tags validations:
https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-tagging.html

Check if all acccess modifiers are all right, private, protected, etc.

Please Report any limitations that you found so that I can add validations.

Use the annotation @NotNull

Think about removing jaxb

Check about license, and legal matters

Check what does it mean to develop open source, what to expect, what is good,
what is bad, what is expected from me, what is not expected.

List capabilities of the lib/pre-signed post

try to remove dependencies, amazon, etc

search about what to think when doing a lib, how should be the methods for the person using it, 
the construcutor, the builder, and any other things to take in consideration  code wise.

See if I can watch the pages of doc from amazon in case they change something.

check if I should remove UTC and go for the default system time zone


FreeTextPostParam
- gives total freedom for adding valuedConditions and params used

## AWS erros and solutions

```aidl
The provided token is malformed or otherwise invalid.
```
or
```aidl
The AWS Access Key Id you provided does not exist in our records
```
- Double check the AWS_SESSION_TOKEN, AWS_SESSION_SECRET and AWS_SESSION_TOKEN, one or more values might be wrong
- Make sure you are not mixing AWS_KEY and AWS_SECRET with session credentials 
- ASIAUNVUIU7WKO5WTRO2
creating non offical libraries, what to take care, can aws sue me, naming, should I say it is non official, etc

Add warn when date is too long

try to remove aws dependencies?

HAVE A look in well know libraries souce code to see what they are using
@nonull? etc

- Should I think about put as well? or naming the library with a more generic name?
- Add final where it should be


- Test logging in ECS
- Add id to pre signed post generation to get meaningfull loggings, userId, etc
- Have one more look in the logs, Input validation failures e.g. protocol violations, unacceptable encodings, invalid parameter names and values


