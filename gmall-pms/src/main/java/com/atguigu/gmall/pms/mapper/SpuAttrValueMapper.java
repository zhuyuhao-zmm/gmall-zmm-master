package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu属性值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-05-16 14:35:19
 */
@Mapper
public interface SpuAttrValueMapper extends BaseMapper<SpuAttrValueEntity> {

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuId(Long spuId);

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuIdAndGId(@Param("spuId") Long spuId, @Param("groupId") Long id);
}
