package cn.zlianpay.carmi.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class CardsVo {

    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 对应商品id
     */
    private String productName;

    /**
     * 卡密
     */
    private String cardInfo;

    /**
     * 卡密状态
     */
    private Integer status;

    /**
     *  售卡类型
     */
    private Integer sellType;

    /**
     * 总数
     */
    private Integer number;

    /**
     * 售出数量
     */
    private Integer sellNumber;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}
