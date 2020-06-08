package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.feign.fallback.GmallSmsFallback;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "sms-service", fallback = GmallSmsFallback.class)
public interface GmallSmsClient extends GmallSmsApi {

}
