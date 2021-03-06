package cn.zlianpay.carmi.controller;

import cn.zlianpay.common.core.web.*;
import cn.zlianpay.carmi.vo.CardsDts;
import cn.zlianpay.carmi.vo.CardsVo;
import cn.zlianpay.common.core.web.*;
import cn.zlianpay.common.core.annotation.OperLog;
import cn.zlianpay.carmi.entity.Cards;
import cn.zlianpay.carmi.service.CardsService;
import cn.zlianpay.products.entity.Classifys;
import cn.zlianpay.products.entity.Products;
import cn.zlianpay.products.service.ClassifysService;
import cn.zlianpay.products.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡密管理
 * Created by Panyoujie on 2021-03-28 00:33:15
 */
@Controller
@RequestMapping("/carmi/cards")
public class CardsController extends BaseController {

    @Autowired
    private CardsService cardsService;

    @Autowired
    private ClassifysService classifysService;

    @Autowired
    private ProductsService productsService;

    @RequiresPermissions("carmi:cards:view")
    @RequestMapping()
    public String view(Model model) {
        List<Classifys> classifysList = classifysService.listAll(null);
        model.addAttribute("classifysList", classifysList);
        return "carmi/cards.html";
    }

    @RequiresPermissions("carmi:cards:view")
    @RequestMapping("/add")
    public String ViewAdd(Model model) {
        List<Classifys> classifysList = classifysService.listAll(null);
        model.addAttribute("classifysList", classifysList);
        return "carmi/cards-add.html";
    }

    /**
     * 分页查询卡密
     */
    @RequiresPermissions("carmi:cards:list")
    @OperLog(value = "卡密管理", desc = "分页查询")
    @ResponseBody
    @RequestMapping("/page")
    public PageResult<CardsVo> page(HttpServletRequest request) {
        PageParam<Cards> pageParam = new PageParam<>(request);
        List<Cards> records = cardsService.page(pageParam, pageParam.getWrapper()).getRecords();
        List<CardsVo> cardsVoList = records.stream().map((cards) -> {
            CardsVo cardsVo = new CardsVo();
            BeanUtils.copyProperties(cards, cardsVo);
            // 查出商品的名称
            Products products = productsService.getById(cards.getProductId());
            cardsVo.setProductName(products.getName());
            return cardsVo;
        }).collect(Collectors.toList());
        return new PageResult<>(cardsVoList, pageParam.getTotal());
    }

    /**
     * 查询全部卡密
     */
    @RequiresPermissions("carmi:cards:list")
    @OperLog(value = "卡密管理", desc = "查询全部")
    @ResponseBody
    @RequestMapping("/list")
    public JsonResult list(HttpServletRequest request) {
        PageParam<Cards> pageParam = new PageParam<>(request);
        return JsonResult.ok().setData(cardsService.list(pageParam.getOrderWrapper()));
    }

    /**
     * 根据id查询卡密
     */
    @RequiresPermissions("carmi:cards:list")
    @OperLog(value = "卡密管理", desc = "根据id查询")
    @ResponseBody
    @RequestMapping("/get")
    public JsonResult get(Integer id) {
        return JsonResult.ok().setData(cardsService.getById(id));
    }

    /**
     * 添加卡密
     */
    @RequiresPermissions("carmi:cards:save")
    @OperLog(value = "卡密管理", desc = "添加", param = false, result = true)
    @ResponseBody
    @RequestMapping("/save")
    public JsonResult save(CardsDts cardsDts) {

        Products products = productsService.getById(cardsDts.getProductId());
        if (products.getSellType() != cardsDts.getSellType()) { // 重复销售
            return JsonResult.error("本商品为重复售卡类型、请使用重复售卡来导入卡密！！");
        }

        return cardsService.addCards(cardsDts);
    }

    /**
     * 修改卡密
     */
    @RequiresPermissions("carmi:cards:update")
    @OperLog(value = "卡密管理", desc = "修改", param = false, result = true)
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(Cards cards) {
        if (cardsService.updateById(cards)) {
            return JsonResult.ok("修改成功");
        }
        return JsonResult.error("修改失败");
    }

    /**
     * 删除卡密
     */
    @RequiresPermissions("carmi:cards:remove")
    @OperLog(value = "卡密管理", desc = "删除", result = true)
    @ResponseBody
    @RequestMapping("/remove")
    public JsonResult remove(Integer id) {
        if (cardsService.removeById(id)) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }

    /**
     * 批量添加卡密
     */
    @RequiresPermissions("carmi:cards:save")
    @OperLog(value = "卡密管理", desc = "批量添加", param = false, result = true)
    @ResponseBody
    @RequestMapping("/saveBatch")
    public JsonResult saveBatch(@RequestBody List<Cards> list) {
        if (cardsService.saveBatch(list)) {
            return JsonResult.ok("添加成功");
        }
        return JsonResult.error("添加失败");
    }

    /**
     * 批量修改卡密
     */
    @RequiresPermissions("carmi:cards:update")
    @OperLog(value = "卡密管理", desc = "批量修改", result = true)
    @ResponseBody
    @RequestMapping("/updateBatch")
    public JsonResult updateBatch(@RequestBody BatchParam<Cards> batchParam) {
        if (batchParam.update(cardsService, "id")) {
            return JsonResult.ok("修改成功");
        }
        return JsonResult.error("修改失败");
    }

    /**
     * 批量删除卡密
     */
    @RequiresPermissions("carmi:cards:remove")
    @OperLog(value = "卡密管理", desc = "批量删除", result = true)
    @ResponseBody
    @RequestMapping("/removeBatch")
    public JsonResult removeBatch(@RequestBody List<Integer> ids) {
        if (cardsService.removeByIds(ids)) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }

    /**
     * 导出指定的数据
     */
    @RequiresPermissions("carmi:cards:list")
    @OperLog(value = "卡密管理", desc = "导出指定的数据")
    @ResponseBody
    @RequestMapping("/export")
    public JsonResult export(Integer productId, Integer status) {
        String export = cardsService.export(productId, status);
        return JsonResult.ok().setData(export);
    }

}
