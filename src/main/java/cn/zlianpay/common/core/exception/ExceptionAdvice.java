package cn.zlianpay.common.core.exception;

import cn.zlianpay.common.core.web.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 类名称：ExceptionAdvice<br>
 * 类描述：异常处理<br>
 * @version v1.0
 *
 */
@ControllerAdvice
@ResponseBody
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger("exceptionAdvice");

    @ResponseStatus(HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE)
    @ExceptionHandler(RateLimitException.class)
    public JsonResult handleRateLimitException(RateLimitException e) {
        logger.error("接口限流了", e);
        return JsonResult.error("哇~~太火爆了，请刷新尝试！");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(SameUrlDataRequestException.class)
    public JsonResult handleSameUrlDataRequestsException(SameUrlDataRequestException e) {
        logger.error("请求太频繁,请勿动重复请求", e);
        return JsonResult.error(429,"请求太频繁,请勿动重复请求");
    }
}
