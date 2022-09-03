package com.su.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.su.gulimall.product.entity.ProductAttrValueEntity;
import com.su.gulimall.product.service.ProductAttrValueService;
import com.su.gulimall.product.vo.AttrRespseVo;
import com.su.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.su.gulimall.product.service.AttrService;
import com.su.common.utils.PageUtils;
import com.su.common.utils.R;


/**
 * 商品属性
 *
 * @author sukun
 * @email 1477264431@qq.com
 * @date 2022-08-18 18:05:10
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities= productAttrValueService.baseAttrlistForSpu(spuId);
        return R.ok().put("data", entities);
    }

    @GetMapping("/{type}/list/{catelogId}")
    public R AttrList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId, @PathVariable String type) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespseVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);
        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId, entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
