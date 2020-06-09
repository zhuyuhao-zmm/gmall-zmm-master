package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.api.vo.ItemSaleVo;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {

    @PostMapping("sms/skubounds/sku/Sales")
    public ResponseVo<Object> saveSkuSales(@RequestBody SkuSaleVo skuSaleVo);


    /**
     * 根据skuId查询sku的营销信息
     * @param skuId
     * @return
     */
    @GetMapping("sms/skubounds/sku/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable("skuId")Long skuId);
}
