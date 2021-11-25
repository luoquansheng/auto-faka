package cn.zlianpay;

import cn.zlianpay.carmi.entity.Cards;
import cn.zlianpay.carmi.service.CardsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjiecode.wxpusher.client.WxPusher;
import com.zjiecode.wxpusher.client.bean.Message;
import com.zjiecode.wxpusher.client.bean.MessageResult;
import com.zjiecode.wxpusher.client.bean.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZlianWebApplicationTests {

    @Autowired
    private CardsService cardsService;

    @Test
    public void contextLoads() throws IOException {
        //region 获取当前项目路径方法
        String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);

        File dir = new File("");
        String projectPath2 = dir.getCanonicalPath();
        System.out.println(projectPath2);
        //endregion

        //region 测试微信推送消息
        // 消息推送文档
        Message message = new Message();
        message.setContent("自助杂货铺" + "登录提醒<br>验证码：<span style='color:red;'>" + "123456" + "</span><br>以上验证密码五分钟内有效，如非本人操作请勿泄露或转发他人。<span><br>");
        message.setContentType(Message.CONTENT_TYPE_HTML);
        message.setUid("UID_n9wwF2qYG3UgrE2FBVW7Nx5rfmQC");
        message.setAppToken("AT_sBVuzPV6yN3gLwMNo4GmXqDnZiZtCzRz");
        Result<List<MessageResult>> result = WxPusher.send(message);
        System.out.println(result.getData().get(0).isSuccess());
        //endregion
    }

    @Test
    public void selectCard(){
        String pid = 8+"";
        int mun = 1;
        List<Cards> card = cardsService.getBaseMapper().selectList(new QueryWrapper<Cards>()
                .eq("status",0)
                .eq("product_id",pid)
                .eq("sell_type",0)
                .orderBy(true,false,"rand()")
                .last("LIMIT "+mun+""));

        System.out.println(card);
    }


    @Autowired
    private JedisPool jedisPool;
    @Test
    public void jredis_set(){
        Jedis jedis = jedisPool.getResource();
        // 添加数据到 set
        jedis.sadd("ip_blacklist",333333+"");
        // 获取set
        Set<String> res = jedis.smembers("ip_blacklist");
        System.out.println(res);
        // 判断set 集合中是否存在
        System.out.println(jedis.sismember("ip_blacklist","333333"));
        // 删除set 集合的某个元素
        jedis.srem("ip_blacklist","333333");
    }

}
