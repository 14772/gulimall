package com.su.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Skus {
    private List<Attr> attr;
    private int countStatus;
    private List<String> descar;
    private BigDecimal discount;
    private Long fullCount;
    private BigDecimal fullPrice;
    private List<Image> images;
    private List<MemberPrice> memberPrice;
    private BigDecimal price;
    private int priceStatus;
    private BigDecimal reducePrice;
    private String skuName;
    private String skuSubtitle;
    private String skuTitle;
}