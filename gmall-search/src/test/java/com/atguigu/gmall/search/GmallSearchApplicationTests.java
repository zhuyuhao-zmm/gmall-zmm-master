package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchAttrVo;
import com.atguigu.gmall.search.feigin.GmallPmsClient;
import com.atguigu.gmall.search.feigin.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    void elasticsearchTest() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    @Test
    public void importData(){
        // 创建索引及映射
        this.elasticsearchRestTemplate.createIndex(Goods.class);
        this.elasticsearchRestTemplate.putMapping(Goods.class);

        //定义分页参数
        Integer pageNum = 1;
        Integer pageSize = 100;

        do{
            //分批获取spu
            ResponseVo<List<SpuEntity>> spuResponseVo = pmsClient.querySpuPage(new PageParamVo(pageNum, pageSize, null));
            List<SpuEntity> spuEntities = spuResponseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)){
                return;
            }

            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkuBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)){
                    //把skuEntity的属性值复制给goods
                    List<Goods> goodlist = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //sku基本信息
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setPrice(skuEntity.getPrice());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setDefaultImage(skuEntity.getDefaultImage());

                        //创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());

                        //品牌
                        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null){
                            goods.setBrandId(skuEntity.getBrandId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        //分类
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null){
                            goods.setCategoryId(skuEntity.getCatagoryId());
                            goods.setCategoryName(categoryEntity.getName());
                        }


                        //销量和库存
                        ResponseVo<List<WareSkuEntity>> listResponseVo = wmsClient.queryWareskuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)){
                            //只要余额 - 锁定数量大于0，就有货
                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                                    wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            //获取所有仓库的销量之和
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales)
                                    .reduce((a, b) -> a + b).get());
                        }

                        List<SearchAttrVo> searchAttrs = new ArrayList<>();

                        //通用属性（检索参数）
                        ResponseVo<List<SpuAttrValueEntity>> AttrResponseVo1 = pmsClient.querySpuAttrValueBySpuId(spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = AttrResponseVo1.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                            searchAttrs.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrVo searchAttrVo = new SearchAttrVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrVo);
                                return searchAttrVo;
                            }).collect(Collectors.toList()));
                        }

                        //销售属性（检索参数）
                        ResponseVo<List<SkuAttrValueEntity>> attrResponseVo = pmsClient.selectSkuAttrValueBySkuId(skuEntity.getId());
                        List<SkuAttrValueEntity> attrValueEntities = attrResponseVo.getData();
                        if (!CollectionUtils.isEmpty(attrValueEntities)){
                            searchAttrs.addAll(
                                    attrValueEntities.stream().map(attrValueEntity -> {
                                        SearchAttrVo searchAttrVo = new SearchAttrVo();
                                        BeanUtils.copyProperties(attrValueEntity, searchAttrVo);
                                        return searchAttrVo;
                                    }).collect(Collectors.toList())
                            );
                        }

                        goods.setSearchAttrs(searchAttrs);

                        return goods;
                    }).collect(Collectors.toList());

                    //批量插入索引
                    goodsRepository.saveAll(goodlist);
                }
            });
            //每遍历一次
            pageNum++;
            //获取当前页的记录
            pageSize = spuEntities.size();
        }while(pageSize == 100);
    }

}
