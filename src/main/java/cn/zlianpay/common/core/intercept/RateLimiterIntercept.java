package cn.zlianpay.common.core.intercept;


import cn.zlianpay.common.core.annotation.RateLimit;
import cn.zlianpay.common.core.exception.RateLimitException;
import cn.zlianpay.common.core.utils.RedisLimiterUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 限流的拦器
 */
@Component
public class RateLimiterIntercept implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod=(HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            /**
             * 首先获取方法上的注解
             */
            RateLimit rateLimit = AnnotationUtils.findAnnotation(method, RateLimit.class);
            //方法上没有标注该注解，尝试获取类上的注解
            if (Objects.isNull(rateLimit)){
                //获取类上的注解
                rateLimit = AnnotationUtils.findAnnotation(handlerMethod.getBean().getClass(), RateLimit.class);
            }
            //没有标注注解，放行
            if (Objects.isNull(rateLimit))
                return true;

            //尝试获取令牌，如果没有令牌了
            if (!RedisLimiterUtils.tryAcquire(request.getRequestURI(),rateLimit.capacity(),rateLimit.rate())){
                //抛出请求超时的异常
                throw new RateLimitException("太火爆了，限流了");
            }
        }
        return true;
    }
}