package cn.zlianpay.common.core.aspect;

import cn.zlianpay.common.core.RedisConstant;
import cn.zlianpay.common.core.annotation.SameUrlData;
import cn.zlianpay.common.core.exception.SameUrlDataRequestException;
import cn.zlianpay.common.core.redis.RedisUtils;
import cn.zlianpay.common.core.utils.UserAgentGetter;
import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Aspect
@Component                         // extends  HandlerInterceptorAdapter
public class SameUrlDataInterceptorAspect {
    private static final Logger logger = LoggerFactory.getLogger(SameUrlDataInterceptorAspect.class);

    @Autowired
    private JedisPool jedisPool;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void methodPointcut() {
    }

    /**
     * 拦截器具体实现
     *
     * @param pjp
     * @return JsonResult（被拦截方法的执行结果，或需要登录的错误提示。）
     */
    @Around("methodPointcut()") //指定拦截器规则；也可以直接把“execution(* com.xjj.........)”写进这里
    public Object Interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod(); //获取被拦截的方法
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (method.isAnnotationPresent(SameUrlData.class)) {
            String methodName = method.getName(); //获取被拦截的方法名
            String ip = UserAgentGetter.getIp(request);
            logger.info("请求getRequestURI：{}", request.getRequestURI());
            logger.info("请求getContextPath：{}", request.getContextPath());
            String url = request.getRequestURI().replaceFirst(request.getContextPath(), "");
            logger.info("请求url：{}", url);
            //if (!StringUtils.startsWith(url, "/app/") && !StringUtils.startsWith(url, "/api/")) {
            //    return pjp.proceed();
            //}
            logger.info("请求方法：{}", methodName);

            if (repeatDataValidator(request)) {
                //请求数据相同
                String message = String.format("请求太频繁,请勿动重复请求！ [%s] 请稍后", ip + url);
                throw new SameUrlDataRequestException(message);
            }
        }
        // 调用目标方法
        Object obj = pjp.proceed();
        return obj;
    }

    /**
     * 验证同一个url数据是否相同提交,相同返回true
     *
     * @param httpServletRequest
     * @return
     */
    public boolean repeatDataValidator(HttpServletRequest httpServletRequest) {
        //获取请求参数map
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        Iterator<Map.Entry<String, String[]>> it = parameterMap.entrySet().iterator();
        String token = "";
        String ip = UserAgentGetter.getIp(httpServletRequest);
        Map<String, String[]> parameterMapNew = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, String[]> entry = it.next();
            if (!entry.getKey().equals("timeStamp") && !entry.getKey().equals("sign")) {
                //去除sign和timeStamp这两个参数，因为这两个参数一直在变化
                parameterMapNew.put(entry.getKey(), entry.getValue());
                if (entry.getKey().equals("token")) {
                    token = entry.getValue()[0];
                }
            }
        }

        //过滤过后的请求内容
        String params = JSONObject.toJSONString(parameterMapNew);

        logger.info("params==========={}",params);

        String url = httpServletRequest.getRequestURI();
        Map<String, String> map = new HashMap<>();
        //key为接口，value为参数
        map.put(url, params);
        String nowUrlParams = map.toString();
        String redisKey = ip.replaceAll("\\.","") + url;
        String preUrlParams = jedisPool.getResource().get(redisKey);
        if (preUrlParams == null) {
            //如果上一个数据为null,表示还没有访问页面
            //存放并且设置有效期，5秒
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.setex(redisKey, 5L, nowUrlParams);
                return false;
            }
        } else {//否则，已经访问过页面
            if (preUrlParams.equals(nowUrlParams)) {
                //如果上次url+数据和本次url+数据相同，则表示重复添加数据
                //重复了还不停请求，每次延期5秒。
                token = ip.replaceAll("\\.","");
                Jedis jedis = jedisPool.getResource();
                jedis.setex(redisKey, 5L, nowUrlParams);
                ipBlacklist(ip);
                return true;
            } else {//如果上次 url+数据 和本次url加数据不同，则不是重复提交
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.setex(redisKey, 5L, nowUrlParams);
                    return false;
                }
            }
        }
    }

    // 频率在 20秒内请求提交 10 次视为攻击，直接拉黑
    private void ipBlacklist(String ip){
        if(RedisConstant.USER_IP.exists(ip)){
            int count = Integer.valueOf(RedisConstant.USER_IP.getStr(ip));
            count++;
            if(count >= 10){
                Jedis jedis = jedisPool.getResource();
                jedis.sadd(RedisConstant.IP_BLACKLIST,ip);
            }else
                RedisConstant.USER_IP.setStr(ip,count+"");
        }else{
            RedisConstant.USER_IP.setStr(ip,1+"");
            RedisConstant.USER_IP.expireTime(ip,20);
        }
    }
}
