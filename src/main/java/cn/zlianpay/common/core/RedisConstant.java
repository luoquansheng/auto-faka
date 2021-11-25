package cn.zlianpay.common.core;

import cn.zlianpay.common.core.redis.RedisUtils;

public class RedisConstant {
    private static final long redisController = 1L;
    /**消息Redis缓存*/
    /////////////////////  缓存KEY  /////////////////////////////
    public static RedisUtils USER_IP = new RedisUtils("user_ip:");

    public static final String IP_BLACKLIST= "ip_blacklist";

}
