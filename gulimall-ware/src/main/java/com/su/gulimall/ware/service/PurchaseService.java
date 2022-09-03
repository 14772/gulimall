package com.su.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.su.common.utils.PageUtils;
import com.su.gulimall.ware.entity.PurchaseEntity;
import com.su.gulimall.ware.vo.MergeVo;
import com.su.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author sukun
 * @email 1477264431@qq.com
 * @date 2022-08-18 21:33:54
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo vo);

    void receivedPurchase(List<Long> ids);

    void done(PurchaseDoneVo vo);
}

