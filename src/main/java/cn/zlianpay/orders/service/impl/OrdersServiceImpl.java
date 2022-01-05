package cn.zlianpay.orders.service.impl;

import cn.zlianpay.common.core.utils.*;
import cn.zlianpay.common.core.web.PageParam;
import cn.zlianpay.common.core.web.PageResult;
import cn.zlianpay.dashboard.DateStrUtil;
import cn.zlianpay.settings.entity.Coupon;
import cn.zlianpay.settings.mapper.CouponMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.zlianpay.orders.mapper.OrdersMapper;
import cn.zlianpay.orders.entity.Orders;
import cn.zlianpay.orders.service.OrdersService;
import cn.zlianpay.products.entity.Products;
import cn.zlianpay.products.mapper.ProductsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 订单表服务实现类
 * Created by Panyoujie on 2021-03-29 16:24:28
 */
@Service
@Transactional
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public PageResult<Orders> listPage(PageParam<Orders> page) {
        List<Orders> records = baseMapper.listPage(page);
        return new PageResult<>(records, page.getTotal());
    }

    @Override
    public List<Orders> listAll(Map<String, Object> page) {
        return baseMapper.listAll(page);
    }

    /**
     * 创建订单
     *
     * @param productId 商品id
     * @param number    购买数量
     * @param payType   支付驱动
     * @param request
     * @return
     */
    @Override
    public Map<String, String> buy(Integer productId, Integer number, String contact, Integer couponId, String payType, String password, HttpServletRequest request) {

        // 记得 map 第二个泛型是数组 要取 第一个元素 即[0]
        Map<String, String> params = RequestParamsUtil.getParameterMap(request);

        Products products = productsMapper.selectById(productId);

        Map<String, String> map = new HashMap<>();

        Orders orders = new Orders();

        /**
         * 是否为附加信息
         */
        Integer isCustomize = products.getIsCustomize();
        if (isCustomize == 1) {
            String customizeInput = products.getCustomizeInput();
            String[] customize = customizeInput.split("\\n");

            StringBuilder attachInfo = new StringBuilder();
            for (String s : customize) {
                String[] split = s.split("=");
                String s1 = params.get(split[0]); // get到提交过来的字段内容
                attachInfo.append(split[1]).append("=").append(s1).append(",");
            }

            // 去除多余尾部的逗号
            String result = attachInfo.deleteCharAt(attachInfo.length() - 1).toString();
            orders.setAttachInfo(result);
        }

        orders.setPrice(products.getPrice());
        orders.setStatus(0); // 1 为支付，0未支付
        orders.setProductId(productId);
        orders.setPayType(payType);
        orders.setNumber(number); // 订单数量
        orders.setContact(contact);
        if (!StringUtils.isEmpty(password)) { // 不为空的时候
            orders.setPassword(password);
        }

        // 得到商品的实际支付金额
        BigDecimal multiply = products.getPrice().multiply(new BigDecimal(number));

        // 判断是不是批发商品
        if (products.getIsWholesale() == 1) {
            String wholesale = products.getWholesale();
            String[] split = wholesale.split("\\n");
            for (String s : split) {
                String[] split1 = s.split("=");
                if (number >= Integer.parseInt(split1[0])) {
                    multiply = new BigDecimal(split1[1]).multiply(new BigDecimal(number));
                }
            }
        }

        if (!StringUtils.isEmpty(couponId)) {
            orders.setIsCoupon(1); // 1 为使用优惠了
            orders.setCouponId(couponId);

            /**
             * 查出对应的优惠券
             */
            Coupon coupon = couponMapper.selectById(couponId);

            if (coupon.getDiscountType() == 0) { // 满减优惠券
                if (multiply.compareTo(coupon.getFullReduction()) > -1) { // 判断实际支付金额是否满足满减的对滴金额
                    // 得到满减后的价格
                    BigDecimal bigDecimal = multiply.subtract(coupon.getDiscountVal()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                    orders.setMoney(bigDecimal);
                } else {
                    orders.setMoney(multiply);
                }
            }
            if (coupon.getDiscountType() == 1)  { // 折扣优惠券
                if (multiply.compareTo(coupon.getFullReduction()) > -1) { // 判断实际支付金额是否满足满减的对滴金额
                    // 得到满减后的价格
                    BigDecimal bigDecimal = multiply.subtract(multiply.multiply(toPoint(coupon.getDiscountVal().toString()).setScale(2, BigDecimal.ROUND_HALF_DOWN)))
                            .setScale(2, BigDecimal.ROUND_HALF_DOWN);
                    orders.setMoney(bigDecimal);
                } else {
                    orders.setMoney(multiply);
                }
            }
            if (coupon.getDiscountType() == 2)  { // 商品指定单价券
                if (multiply.compareTo(coupon.getFullReduction()) > -1) { // 判断实际支付金额是否满足满减的对滴金额
                    // 得到满减后的价格
                    orders.setMoney(coupon.getDiscountVal().multiply(new BigDecimal(number)));
                    //multiply = new BigDecimal(orders.getNumber());
                } else {
                    orders.setMoney(multiply);
                }
            }

            Coupon coupon1 = new Coupon();
            coupon1.setId(coupon.getId());

            // 判断优惠券 是一次性还是重复
            if (coupon.getType() == 0) { // 一次性
                coupon1.setStatus(1); // 设置为已使用
                couponMapper.updateById(coupon1);
            } else { // 重复使用
                if (coupon.getCountAll() != 1) { // 表示最后一次使用
                    coupon1.setCountAll(coupon.getCountAll() - 1);
                    coupon1.setCountUsed(coupon.getCountUsed() + 1);
                } else {
                    coupon1.setCountAll(coupon.getCountAll() - 1);
                    coupon1.setCountUsed(coupon.getCountUsed() + 1);
                    coupon1.setStatus(1); // 设置为已使用
                }
                couponMapper.updateById(coupon1);
            }
        } else {
            orders.setMoney(multiply);
            orders.setIsCoupon(0); // 1 为使用优惠了
        }

        System.out.println("订单数：" + number);
        System.out.println("总价：" + multiply);
        System.out.println("实际支付价格：" + orders.getMoney());

        /**
         * 判断下是不是电子邮件
         * 如果是测写入到email entity
         */
        if (FormCheckUtil.isEmail(contact)) {
            orders.setEmail(contact);
        }

        orders.setMember("TUD" + DateUtil.subData() + StringUtil.getRandomString(6));
        orders.setCreateTime(new Date());

        UserAgentGetter agentGetter = new UserAgentGetter(request);
        orders.setIp(agentGetter.getIp());
        orders.setDevice(agentGetter.getDevice());

        baseMapper.insert(orders);

        map.put("total_price", multiply.toString());
        map.put("money", orders.getMoney().toString());
        map.put("member", orders.getMember());

        return map;
    }

    public static BigDecimal toPoint(String percent) {
        percent = percent.replace("%", "");
        Double f = Double.valueOf(percent) / 10;
        //  Float f = Float.valueOf(percent) / 10;
        BigDecimal decimal = new BigDecimal(f);
        return decimal;
    }

    public static BigDecimal toPoint100(String percent) {
        percent = percent.replace("%", "");
        Double f = Double.valueOf(percent) / 100;
        //  Float f = Float.valueOf(percent) / 100;
        BigDecimal decimal = new BigDecimal(f);
        return decimal;
    }

    @Override
    public Orders selectByMember(String member) {
        Orders orders = baseMapper.selectOne(new QueryWrapper<Orders>().eq("member", member));
        return orders;
    }

    @Override
    public boolean clearRemove() {
        QueryWrapper<Orders> status = new QueryWrapper<>();
        status.eq("status", 0);
        if (baseMapper.delete(status) >= 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean clearAllRemove() {
        Date startDayTime = DateStrUtil.getStartDayTime(-+7);
        QueryWrapper queryWrapper = getQueryWrapper(startDayTime);

        List<Orders> list = this.list(queryWrapper);
        List<Integer> ids = new ArrayList<>();
        for (Orders orders : list) {
            ids.add(orders.getId());
        }

        boolean removeByIds = this.removeByIds(ids);
        return removeByIds;
    }

    @Override
    public boolean deleteById(Integer id) {
        return this.removeById(id);
    }

    /**
     * 根据时间查询七天前的数据
     *
     * @param StartTime 开始的时间
     * @return queryWrapper
     */
    public static QueryWrapper getQueryWrapper(Date StartTime) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DayStartTime = formatter.format(StartTime);

        //查询条件为时间范围
        queryWrapper.apply("UNIX_TIMESTAMP(create_time) <= UNIX_TIMESTAMP('" + DayStartTime + "')");

        return queryWrapper;
    }
}
