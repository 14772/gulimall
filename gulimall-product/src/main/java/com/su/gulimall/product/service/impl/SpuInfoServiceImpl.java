package com.su.gulimall.product.service.impl;

import com.su.common.to.SkuReductionTo;
import com.su.common.to.SpuBoundTo;
import com.su.common.utils.R;
import com.su.gulimall.product.entity.*;
import com.su.gulimall.product.feign.CouponFeignService;
import com.su.gulimall.product.service.*;
import com.su.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.common.utils.PageUtils;
import com.su.common.utils.Query;

import com.su.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存 SPU 基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);
        //2、保存 SPU 描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);
        //3、保存 SPU 图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);
        //4、保存 SPU 规格参数 pms_product_attr_value
        List<BaseAttr> baseAttrs = vo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
                ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
                valueEntity.setAttrId(attr.getAttrId());
                AttrEntity byId = attrService.getById(attr.getAttrId());
                valueEntity.setAttrName(byId.getAttrName());
                valueEntity.setAttrValue(attr.getAttrValues());
                valueEntity.setQuickShow(attr.getShowDesc());
                valueEntity.setSpuId(infoEntity.getId());
                return valueEntity;
            }).toList();
            productAttrValueService.saveProductAttr(productAttrValueEntities);
        }
        //5、 保存 SPU 积分信息 gulimall_sms.sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R saveSpuBounds = couponFeignService.saveSpuBounds(spuBoundTo);
        if (saveSpuBounds.getCode() != 0) {
            log.error("远程保存SPU积分信息失败");
        }
        //6、保存 SPU 对应的 SKU 信息：
        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(item -> {
                //6.1、SKU 基本信息 pms_sku_info
                String defaultImage = "";
                for (Image img : item.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImage = img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatelogId(infoEntity.getCatelogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                //6.2、SKU 图片信息 pms_spu_images
                Long skuId = skuInfoEntity.getSkuId();
                List<Image> imageList = item.getImages();
                if (!CollectionUtils.isEmpty(imageList)) {
                    List<SkuImagesEntity> imagesEntities = imageList.stream().filter(
                            img -> !StringUtils.hasLength(img.getImgUrl())
                    ).map(img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setImgUrl(img.getImgUrl());
                        skuImagesEntity.setDefaultImg(img.getDefaultImg());
                        return skuImagesEntity;
                    }).toList();
                    skuImagesService.saveBatch(imagesEntities);
                }
                //6.3、SKU 销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                if (!CollectionUtils.isEmpty(attrs)) {
                    List<SkuSaleAttrValueEntity> attrValueEntities = attrs.stream().map(attr -> {
                        SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, attrValueEntity);
                        attrValueEntity.setSkuId(skuId);
                        return attrValueEntity;
                    }).toList();
                    skuSaleAttrValueService.saveBatch(attrValueEntities);
                }
                //6.4、SKU 优惠满减信息：sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    R saveSkuReduction = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (saveSkuReduction.getCode() != 0) {
                        log.error("远程保存SKU优惠满减信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.hasLength(key)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasLength(key) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.hasLength(key) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

}