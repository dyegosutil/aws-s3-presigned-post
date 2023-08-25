package mendes.sutil.dyego.awspresignedpost.presigned;

public class PreSignedFreeTextPost {
    private final String xAmzSignature;
    private final String policy;

    public PreSignedFreeTextPost(final String signature, final String policy) {
        this.xAmzSignature = signature;
        this.policy = policy;
    }

    public String getxAmzSignature() {
        return xAmzSignature;
    }

    public String getPolicy() {
        return policy;
    }
}
