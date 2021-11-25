package cn.zlianpay.common.core.exception;

public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = -5782852372678913439L;

    public RateLimitException(String message) {
        super(message);
    }
}