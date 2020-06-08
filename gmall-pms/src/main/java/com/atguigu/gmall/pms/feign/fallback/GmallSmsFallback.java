package com.atguigu.gmall.pms.feign.fallback;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import org.springframework.stereotype.Component;

@Component
public class GmallSmsFallback implements GmallSmsClient {

    @Override
    public ResponseVo<Object> saveSkuSales(SkuSaleVo skuSaleVo) {
        return ResponseVo.fail("保存营销信息失败");
    }
}
