package cn.zlianpay.common.core.redis;

import cn.zlianpay.common.core.spring.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.*;
//import redis.clients.jedis.params.geo.GeoRadiusParam;

import java.util.*;

/**
 * redis相关操作工具类
 *
 * @author Administrator
 */
public class RedisUtils {
    Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    private String key;

    private JedisPool jedisPool;

    public RedisUtils(String key) {
        this.key = key;
        this.jedisPool = SpringUtils.getBean(JedisPool.class);    // 获取redis连接池的地址
    }

    /**
     * 判断key是否存在
     *
     * @param
     * @return boolean 是否存在key
     * @version 1.0.0
     */
    public boolean exists(String field) {
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis.exists(key + field)) {
                result = true;
            }
        } catch (Exception e) {
            logger.debug(key + field + ": " + e + "1");
        } finally {
            if (jedis != null)
                jedis.close();
        }

        return result;
    }

    /**
     * 设置 key过期时间
     *
     * @param seconds 过期时间 ，单位 秒
     * @return boolean 是否存在key
     * @version 1.0.0
     */
    public boolean expireTime(String field,int seconds) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.expire(key + field, seconds);
            if (result > 0) {
                return true;
            }
        } catch (Exception e) {
            logger.debug(key + field + ": " + e + "1");
        } finally {
            if (jedis != null)
                jedis.close();
        }

        return false;
    }

    /**
     * 1. 设置string 类型的数据
     * @param value 值 value
     * @return
     * @version 1.0.0
     */
    public Long setStr(String field,String value) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.setnx(key + field,value);
            } catch (Exception e) {
                logger.debug(key + field + ": " + e + "3");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 2. 获取string 类型的存储
     * @param field
     * @return
     */
    public String getStr(String field) {
        String result = "";
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.get(key + field);
            } catch (Exception e) {
                logger.debug(key + field + ": " + e + "3");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 3. 删除指定 key
     * @param field
     * @return
     */
    public Long del(String field) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.del(key + field);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "6");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }


    /**
     * 1.判断当前key下的指定字段是否存在
     *
     * @param field hash的字段
     * @return boolean 是否存在指定字段
     * @version 1.0.0
     */
    public boolean hExists(String field) {
        boolean result = false;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hexists(key, field);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "2");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 2.将哈希表 key中的字段 field的值设为 value
     *
     * @param field,value 字段 field,值 value
     * @return
     * @version 1.0.0
     */
    public Long hSet(String field, String value) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hset(key, field, value);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "3");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 3.获取哈希表key中指定字段的值
     *
     * @param field 字段 field
     * @return result 哈希表中指定字段的值
     * @version 1.0.0
     */
    public String hGet(String field) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                if (jedis.hexists(key, field))
                    result = jedis.hget(key, field);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "4");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 4.获取在哈希表 key的所有字段和值
     *
     * @param del 是否并删除指定key的所有值
     * @return result 指定 key 的所有字段和值
     * @version 1.0.0
     */
    public Map<String, String> hGetAll(boolean del) {
        Map<String, String> result = new HashMap<String, String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            Transaction tr = null;
            try {
                jedis = jedisPool.getResource();
                tr = jedis.multi();
                Response<Map<String, String>> response = tr.hgetAll(key);
                if (del) {
                    tr.del(key);
                }
                tr.exec();

                result = response.get();
            } catch (Exception e) {
                logger.debug(key + ": " + e + "5");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 5.删除哈希表key中一个或多个字段
     *
     * @param fields 一个或多个哈希表字段
     * @return result 影响的个数？
     * @version 1.0.0
     */
    public Long hDel(String... fields) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hdel(key, fields);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "6");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 6.为哈希表 key中的指定字段的整数值加上增量 increment
     *
     * @param fields,value 指定字段,增量 increment
     * @return result 影响的个数？
     * @version 1.0.0
     */
    public Long hIncrBy(String field, Long value) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hincrBy(key, field, value);

            } catch (Exception e) {
                logger.debug(key + ": " + e + "7");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 7.为哈希表 key中的指定字段的浮点数值加上增量 increment
     *
     * @param fields,value 指定字段,增量 increment
     * @return result 影响的个数？
     * @version 1.0.0
     */
    public Double hIncrByFloat(String field, Double value) {
        Double result = 0.0;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hincrByFloat(key, field, value);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "8");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 8.获取哈希表中所有的字段
     *
     * @param
     * @return result 哈希表中所有的字段
     * @version 1.0.0
     */
    public Set<String> hKeys() {
        Set<String> result = new HashSet<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hkeys(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "9");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 9.获取哈希表中字段的数量
     *
     * @param
     * @return result 字段的数量
     * @version 1.0.0
     */
    public Long hLen() {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hlen(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "10");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 10.获取所有给定字段的值
     *
     * @param fields 给定字段
     * @return result 所有给定字段的值
     * @version 1.0.0
     */
    public List<String> hMGet(String... fields) {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hmget(key, fields);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "11");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 11.同时将多个 field-value(域-值)对设置到哈希表 key中
     *
     * @param hash 多个 field-value(域-值)
     * @return result
     * @version 1.0.0
     */
    public String hMSet(Map<String, String> hash) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hmset(key, hash);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "12");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 12.获取哈希表key中所有值
     *
     * @param
     * @return result 哈希表key中所有值
     * @version 1.0.0
     */
    public List<String> hVals() {
        List<String> result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hvals(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "13");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 13.只有在字段 field不存在时，设置哈希表字段的值
     *
     * @param field,value 字段，值
     * @return result
     * @version 1.0.0
     */
    public Long hSetNx(String field, String value) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.hsetnx(key, field, value);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "14");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 1.为已存在的列表添加值
     *
     * @param values 一个或多个值
     * @return result
     * @version 1.0.0
     */
    public Long rPushx(String... values) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.rpushx(key, values);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "15");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 2.在列表中添加一个或多个值
     *
     * @param values 一个或多个值
     * @return result
     * @version 1.0.0
     */
    public Long rPush(String... values) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.rpush(key, values);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "16");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 3.移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     *
     * @param dstkey 另一个列表的key
     * @return result
     * @version 1.0.0
     */
    public String rPoplPush(String dstkey) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.rpoplpush(key, dstkey);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "17");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 4.移除并获取列表最后一个元素
     *
     * @param
     * @return result 列表最后一个元素
     * @version 1.0.0
     */
    public String rPop() {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.rpop(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "18");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 5.对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     *
     * @param start,stop 区间开始，区间结束
     * @return result
     * @version 1.0.0
     */
    public String lTrim(Long start, Long stop) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.ltrim(key, start, stop);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "19");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 6.通过索引设置列表元素的值
     *
     * @param index,value 位置，值
     * @return result
     * @version 1.0.0
     */
    public String lSet(Long index, String value) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lset(key, index, value);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "20");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 7.移除列表元素
     *
     * @param count,value count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为
     *                    COUNT count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值
     *                    count = 0 : 移除表中所有与VALUE 相等的值。
     * @return result
     * @version 1.0.0
     */
    public Long lRem(Long count, String value) {
        Long result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lrem(key, count, value);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "21");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 8.获取列表指定范围内的元素
     *
     * @param start,stop 范围开始，范围结束
     * @return result 范围内的元素
     * @version 1.0.0
     */
    public List<String> lRange(Long start, Long stop) {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lrange(key, start, stop);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "22");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 9.将一个或多个值插入到已存在的列表头部
     *
     * @param values 一个或多个值
     * @return result
     * @version 1.0.0
     */
    public Long lPushx(String... values) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lpushx(key, values);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "23");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 10.将一个或多个值插入到列表头部
     *
     * @param values 一个或多个值
     * @return result
     * @version 1.0.0
     */
    public Long lPush(String... values) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lpush(key, values);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "24");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 11.移出并获取列表的第一个元素
     *
     * @param
     * @return result 列表的第一个元素
     * @version 1.0.0
     */
    public String lPop() {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lpop(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "25");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 12.获取列表长度
     *
     * @param
     * @return result 列表长度
     * @version 1.0.0
     */
    public Long lLen() {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.llen(key);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "26");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 13.在列表的元素前或者后插入元素
     *
     * @param where,pivot,value BEFORE,AFTER，值，插入值
     * @return result 列表长度，-1，0
     * @version 1.0.0
     *
     */
    // TODO 找不到包 LIST_POSITION ，老板版本jedis 有
    //public Long linsert(LIST_POSITION where, String pivot, String value) {
    //    Long result = 0L;
    //    if (StringUtils.isNotBlank(key)) {
    //        Jedis jedis = null;
    //        try {
    //            jedis = jedisPool.getResource();
    //            result = jedis.linsert(key, where, pivot, value);
    //        } catch (Exception e) {
    //            logger.debug(key + ": " + e + "27");
    //        } finally {
    //            if (jedis != null)
    //                jedis.close();
    //        }
    //    }
    //    return result;
    //}

    /**
     * 14.通过索引获取列表中的元素
     *
     * @param where,pivot,value BEFORE,AFTER，值，插入值
     * @return result 指定索引值的元素。 如果指定索引值不在列表的区间范围内，返回 nil
     * @version 1.0.0
     */
    public String lIndex(Long index) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.lindex(key, index);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "28");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 15.从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param destination,timeout 另外一个列表，超时时间
     * @return result 假如在指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。
     * 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素的值，第二个元素是等待时长。
     * @version 1.0.0
     */
    public String brPoplPush(String destination, Integer timeout) {
        String result = null;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.brpoplpush(key, destination, timeout);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "29");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 16.移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout,keys 超时时间，列表key
     * @return result 指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。
     * 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key ，第二个元素是被弹出元素的值
     * @version 1.0.0
     */
    public List<String> brPop(Integer timeout, String... keys) {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.brpop(timeout, keys);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "30");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 17.移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout,keys 超时时间，列表key
     * @return result 指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。
     * 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key ，第二个元素是被弹出元素的值
     * @version 1.0.0
     */
    public List<String> blPop(Integer timeout, String... keys) {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.blpop(timeout, keys);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "31");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 18.获取列表内所有值
     *
     * @param del 是否并删除
     * @return result 列表所有值
     * @version 1.0.0
     */
    public List<String> lGetAll(boolean del) {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                Transaction tr = jedis.multi();
                Response<List<String>> response = tr.lrange(key, 0, -1);
                if (del) {
                    tr.del(key);
                }
                tr.exec();
                result = response.get();
            } catch (Exception e) {
                logger.debug(key + ": " + e + "32");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 19.获取列表内所有包含str的值
     *
     * @param str,del 某字符串,是否并删除
     * @return result 包含str的值
     * @version 1.0.0
     */
    public List<String> lGetByString(String str, boolean del) {
        List<String> temp = new ArrayList<String>();
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                Transaction tr = jedis.multi();
                Response<List<String>> response = tr.lrange(key, 0, -1);
                tr.exec();
                tr = jedis.multi();
                temp = response.get();
                for (String item : temp) {
                    if (item.indexOf(str) != -1) {
                        result.add(item);
                        if (del)
                            tr.lrem(key, 1, item);
                    }
                }
                tr.exec();
            } catch (Exception e) {
                logger.debug(key + ": " + e + "33");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }

    /**
     * 20.删除包含str的值
     *
     * @param str
     * @return result 包含str的值
     * @version 1.0.0
     */
    public Long lDelByString(String str) {
        Long result = 0L;
        List<String> temp = new ArrayList<String>();
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                Transaction tr = jedis.multi();
                Response<List<String>> response = tr.lrange(key, 0, -1);
                tr.exec();
                tr = jedis.multi();
                temp = response.get();
                for (String item : temp) {
                    if (item.indexOf(str) != -1) {
                        tr.lrem(key, 1, item);
                        result++;
                    }
                }
                tr.exec();
            } catch (Exception e) {
                logger.debug(key + ": " + e + "34");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }


    /**
     * 添加坐标
     * @param longitude     经度
     * @param latitude      纬度
     * @param userId        用户ID
     *
     * return m 表示单位为米
     */
    public Long addReo(Long longitude,Long latitude,String userId) {
        Long result = 0L;
        if (StringUtils.isNotBlank(key)) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                result = jedis.geoadd(key,longitude,latitude,userId);
            } catch (Exception e) {
                logger.debug(key + ": " + e + "35");
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return result;
    }


    /**
     * 查询附近人
     * @param longitude     经度
     * @param latitude      纬度
     * return GeoRadiusResponse
     */
    // TODO 找不到包 GeoRadiusParam.geoRadiusParam()
    // public List<GeoRadiusResponse> geoQuery(Long longitude, Long latitude) {
    //     List<GeoRadiusResponse> geoRadiusResponseList = new ArrayList<>();
    //     if (StringUtils.isNotBlank(key)) {
    //         Jedis jedis = null;
    //         try {
    //             jedis = jedisPool.getResource();
    //             // 200F GeoUnit.KM表示km
    //             geoRadiusResponseList = jedis.georadius(key,longitude,latitude,200F, GeoUnit.KM, GeoRadiusParam.geoRadiusParam().withDist());
    //         } catch (Exception e) {
    //             logger.debug(key + ": " + e + "36");
    //         } finally {
    //             if (jedis != null)
    //                 jedis.close();
    //         }
    //     }
    //     return geoRadiusResponseList;
    // }
}