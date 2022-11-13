package mendes.sutil.dyego.awspresignedpost.result;

public class FreeTextPresignedPost {
    private final String xAmzSignature;
    private final String policy;

    public FreeTextPresignedPost(final String signature, final String policy) {
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
