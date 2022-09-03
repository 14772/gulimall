package com.su.gulimall.ware.service.impl;

import com.su.common.constant.WareConstant;
import com.su.gulimall.ware.entity.PurchaseDetailEntity;
import com.su.gulimall.ware.service.PurchaseDetailService;
import com.su.gulimall.ware.service.WareSkuService;
import com.su.gulimall.ware.vo.MergeVo;
import com.su.gulimall.ware.vo.PurchaseDoneVo;
import com.su.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.common.utils.PageUtils;
import com.su.common.utils.Query;

import com.su.gulimall.ware.dao.PurchaseDao;
import com.su.gulimall.ware.entity.PurchaseEntity;
import com.su.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)) {
            queryWrapper.and((wrapper) -> wrapper.eq("id", key).or().like("assignee_name", key).or().like("assignee_id", key));
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        Integer status = this.getById(purchaseId).getStatus();
        if (status == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || status == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            Long finalPurchaseId = purchaseId;
            Long[] items = vo.getItems();
            List<PurchaseDetailEntity> detailEntities = Arrays.stream(items).map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).toList();
            purchaseDetailService.updateBatchById(detailEntities);
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }
    }

    @Override
    public void receivedPurchase(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(this::getById)
                .filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                        || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    item.setUpdateTime(new Date());
                }).toList();
        // 2. 改变采购单的状态
        this.updateBatchById(purchaseEntities);
        // 3. 改变采购项的状态
        purchaseEntities.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).toList();
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {
        // 改变采购单的状态
        Long id = vo.getId();
        // 改变采购项的状态
        boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        // 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag
                ? WareConstant.PurchaseStatusEnum.FINISHED.getCode()
                : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}