# Aws S3 Presigned Post

Generating S3 post data for uploading files into public access s3 buckets in not included in this library since it is pretty straight forward. 
That is, only the ```key``` and ```file```.

# Features to be added

- Add a way to pass string params and make the lib compatible with any new fields aws might start supporting

# Reference documents

- Post Policy - https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html

# Issues
- include support for/check ```x-ignore-```