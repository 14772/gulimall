package com.su.gulimall.coupon.service.impl;

import com.su.common.to.MemberPrice;
import com.su.common.to.SkuReductionTo;
import com.su.gulimall.coupon.entity.MemberPriceEntity;
import com.su.gulimall.coupon.entity.SkuLadderEntity;
import com.su.gulimall.coupon.service.MemberPriceService;
import com.su.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.common.utils.PageUtils;
import com.su.common.utils.Query;

import com.su.gulimall.coupon.dao.SkuFullReductionDao;
import com.su.gulimall.coupon.entity.SkuFullReductionEntity;
import com.su.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setFullCount(skuLadderEntity.getFullCount());
        skuLadderEntity.setAddOther(skuLadderEntity.getAddOther());
        if (skuLadderEntity.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        //sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if (skuFullReductionEntity.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            this.save(skuFullReductionEntity);
        }
        //sms_member_price
        List<MemberPrice> memberPriceList = skuReductionTo.getMemberPrice();
        if (!CollectionUtils.isEmpty(memberPriceList)) {
            List<MemberPriceEntity> memberPriceEntities = memberPriceList.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(item -> item.getMemberPrice().compareTo(BigDecimal.ZERO) > 0).toList();
            memberPriceService.saveBatch(memberPriceEntities);
        }
    }

}