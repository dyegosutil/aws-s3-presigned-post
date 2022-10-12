# Aws S3 Presigned Post

Generating S3 post data for uploading files into public access s3 buckets is not included in this library since it is pretty straight forward. 
That is, the only parameters necessary are the ```key``` and ```file```.

# Features to be added

- Add a way to pass string params and make the lib compatible with any new fields aws might start supporting

# Reference documents

- Post Policy - https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html
- https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html

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

TODO
Add logging

TODO
Add adapted desc of this text:
The following table describes a list of fields that you can use within a form. Among other fields, there is a signature field that you can use to authenticate requests. There are fields for you to specify the signature calculation algorithm (x-amz-algorithm), the credential scope (x-amz-credential) that you used to generate the signing key, and the date (x-amz-date) used to calculate the signature. Amazon S3 uses this information to re-create the signature. If the signatures match, Amazon S3 processes the request.

TODO
Also important to add:
All this is for authenticated requests

TODO

The {filename} variable does not work for eq. Only for startsWith.
The reason is that in the policy we cannot simply specify "". It has to have a value. Otherwise the signature check will fail. Amazon will check in the end name_of_user_file == "" And the signature will fail. Hence the value has to be passed by the one calling the lib.
For the startWith, it is okay since you will specify at list on character in front of the user file name.

TODO
Add info from this page
https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html

Add list of features
- Helps you to build the pre-signed post with the minimum necessary params so that it will work
- You dont have to build the policies yourself, the builder simplify your work.

## Running locally

To run the integration tests that uploads files so S3, the following environment variables need to be set:

```
AWS_KEY = Your AWS key such as AKIA...
AWS_SECRET = your secret
AWS_BUCKET = The bucket name where the test files must be uploaded to. Ex: 'testbucket'
AWS_REGION = Ex: 'eu-central-1' any regition that can be used with Region.of(). This should be the region for the valid bucket your have configured to test the uploads.
AWS_WRONG_REGION = This should be a bucket which is not the one you have configured the bucket for. Any value that can be used with Region.of() 
```

Also is necessary to remove the @Disabled annotation from the test zzz

TODO
Add good logging and documentation for it so that if they want to set the conditions
them selves they can know what is wrong? Well they can use postman, but put some errors or warnings if possible

TODO
add log of IKIA inside token but without shwoing it full - show only A********Y
If you want to allow the user upload any key use ```withAnyKey()``` and submit as key name ```${filename}```

TODO
Add examples of how to use each one of the options, content type, range, etc.

Check where info should not be null?

how to make your library to be found by searches on google.

TODO 
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