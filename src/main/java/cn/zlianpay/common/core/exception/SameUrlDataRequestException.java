package cn.zlianpay.common.core.exception;

/**
 * @author 类描述:
 * 创建时间:
 */
public class SameUrlDataRequestException extends RuntimeException {

    private static final long serialVersionUID = 8391869486329200571L;

    /**
     *
     * 创建一个新的实例 TokenException.
     * @param message
     */
    public SameUrlDataRequestException(String message) {
        super(message);
    }

}
