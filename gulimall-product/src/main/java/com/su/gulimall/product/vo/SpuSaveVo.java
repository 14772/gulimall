package com.su.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuSaveVo {
    private List<BaseAttr> baseAttrs;
    private Bounds bounds;
    private Long brandId;
    private Long catelogId;
    private List<String> decript;
    private List<String> images;
    private int publishStatus;
    private List<Skus> skus;
    private String spuDescription;
    private String spuName;
    private BigDecimal weight;
}