package com.su.gulimall.product.service.impl;

import com.su.gulimall.product.entity.AttrEntity;
import com.su.gulimall.product.service.AttrService;
import com.su.gulimall.product.vo.AttrGroupWithAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.common.utils.PageUtils;
import com.su.common.utils.Query;

import com.su.gulimall.product.dao.AttrGroupDao;
import com.su.gulimall.product.entity.AttrGroupEntity;
import com.su.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper.eq("catelog_id", catelogId)
            );
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> groupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId)
        );
        return groupEntities.stream().map(item -> {
            AttrGroupWithAttrVo groupVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(item, groupVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(groupVo.getAttrGroupId());
            groupVo.setAttrs(attrs);
            return groupVo;
        }).toList();
    }

}