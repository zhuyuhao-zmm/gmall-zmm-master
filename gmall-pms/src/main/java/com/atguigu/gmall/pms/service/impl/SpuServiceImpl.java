package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuPageByCid(Long cid, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();

        //判断分类是否为空
        if (cid != 0){
            queryWrapper.eq("category_id", cid);
        }

        //判断关键字是否为空
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            queryWrapper.and(wrapper -> wrapper.eq("id", key).or().like("name", key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                queryWrapper
        );
        return new PageResultVo(page);
    }

    @Autowired
    private SpuDescMapper descMapper;

    @Autowired
    private SpuAttrValueService baseService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private GmallSmsClient SmsClient;

    @Autowired
    private SpuDescService descService;

    // 1.2. 保存spu的描述信息 pms_spu_desc
    @Autowired
    private SkuAttrValueService skuAttrValueService;




    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spuVo) {
        /// 1.保存spu相关
        Long spuId = saveSpu(spuVo);

        // 1.1. 保存spu基本信息 pms_spu
        saveSpuDesc(spuVo, spuId);

        // 1.3. 保存spu的规格参数信息
        saveBaseAttr(spuVo, spuId);

        /// 2. 保存sku相关信息
        saveSku(spuVo, spuId);

//        int i = 1/0;
    }




    private void saveSku(SpuVo spuVo, Long spuId) {
        List<SkuVo> skus = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        // 2.1. 遍历保存sku基本信息 pms_sku
        skus.forEach(skuVo -> {
            //设置页面没有传递的参数
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spuVo.getBrandId());
            skuVo.setCatagoryId(spuVo.getCategoryId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            //判断图片列表是否为空
            if (!CollectionUtils.isEmpty(images)){
                //判断默认图片是否为空
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
            }
            skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            // 2.2. 保存sku图片信息 pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    //判断地址和默认图片地址是否相同
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(image, skuVo.getDefaultImage()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                imagesService.saveBatch(skuImagesEntities);
            }

            // 2.3. 保存sku的规格参数（销售属性）pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(attr -> {
                    attr.setSkuId(skuId);
                });

                skuAttrValueService.saveBatch(saleAttrs);
            }

            // 3. 保存营销相关信息，需要远程调用gmall-sms
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            skuSaleVo.setSkuId(skuId);
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            SmsClient.saveSkuSales(skuSaleVo);
        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVO -> {
                spuAttrValueVO.setSpuId(spuId);
                spuAttrValueVO.setSort(0);
                return spuAttrValueVO;
            }).collect(Collectors.toList());
            this.baseService.saveBatch(spuAttrValueEntities);
        }
    }

    private void saveSpuDesc(SpuVo spuVo, Long spuId) {
        List<String> spuImages = spuVo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)){
            String descript = StringUtils.join(spuImages, ",");
            SpuDescEntity spuInfoDescEntity = new SpuDescEntity();
            // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
            spuInfoDescEntity.setSpuId(spuId);
            // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
            spuInfoDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
            this.descMapper.insert(spuInfoDescEntity);
        }

    }

    private Long saveSpu(SpuVo spuVo) {
        spuVo.setPublishStatus(1); // 默认是已上架
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime()); // 新增时，更新时间和创建时间一致
        this.save(spuVo);
        return spuVo.getId();
    }
}

