package com.su.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.su.common.to.SkuReductionTo;
import com.su.common.utils.PageUtils;
import com.su.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author sukun
 * @email 1477264431@qq.com
 * @date 2022-08-18 21:13:15
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

