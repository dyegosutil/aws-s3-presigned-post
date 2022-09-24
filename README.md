# Aws S3 Presigned Post

Generating S3 post data for uploading files into public access s3 buckets is not included in this library since it is pretty straight forward. 
That is, the only parameters necessary are the ```key``` and ```file```.

# Features to be added

- Add a way to pass string params and make the lib compatible with any new fields aws might start supporting

# Reference documents

- Post Policy - https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html
- https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPOST.html

# Issues
- include support for/check ```x-ignore-```
- Test to be done
  - upload with key in policy 
  - upload with key outside policy
- Check https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html to make everything in an url 
- Add formatting standards according to what is accepted by the community
- scan for any vulnerabilities in dependencies or code betterment.
- Use Value Objects

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