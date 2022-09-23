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