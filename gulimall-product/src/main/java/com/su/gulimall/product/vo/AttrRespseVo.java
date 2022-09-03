package com.su.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespseVo extends AttrVo{
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
