package com.su.gulimall.search.controller;

import com.su.common.to.es.SkuEsModel;
import com.su.common.utils.R;
import com.su.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.su.common.exception.BizCodeEnume.PRODUCT_UP_EXCEPTION;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误:{}", e);
            return R.error(PRODUCT_UP_EXCEPTION.getCode(), PRODUCT_UP_EXCEPTION.getMsg());
        }
        return !b ? R.ok() : R.error(PRODUCT_UP_EXCEPTION.getCode(), PRODUCT_UP_EXCEPTION.getMsg());
    }
}
