package cn.zlianpay.common.core.annotation;

import java.lang.annotation.*;

/**
 * @Title: SameUrlData
 * @Description: 自定义注解防止表单重复提交
 * @Auther: xhq
 * @Version: 1.0
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SameUrlData {
 
}