package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {
    public void setValueSelected(List<String> valueSelected){
        if (!CollectionUtils.isEmpty(valueSelected)){
            setAttrValue(StringUtils.join(valueSelected, ","));
        }
    }
}
