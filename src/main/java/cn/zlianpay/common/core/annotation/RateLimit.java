package cn.zlianpay.common.core.annotation;

import java.lang.annotation.*;

/**
 * @Description 限流的注解，标注在类上或者方法上。在方法上的注解会覆盖类上的注解，同@Transactional
 * @Author CJB
 * @Date 2020/3/20 13:36
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 令牌桶的容量，默认100
     * @return
     */
    int capacity() default 50;

    /**
     * 每秒钟默认产生令牌数量，默认10个
     * @return
     */
    int rate() default 10;
}