package com.su.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.su.common.utils.PageUtils;
import com.su.gulimall.ware.entity.WareSkuEntity;
import com.su.common.to.SkuHasStockTo;

import java.util.List;
import java.util.Map;

/**
 * εεεΊε­
 *
 * @author sukun
 * @email 1477264431@qq.com
 * @date 2022-08-18 21:31:17
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);
}

