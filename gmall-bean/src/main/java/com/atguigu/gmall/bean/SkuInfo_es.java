package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfo_es implements Serializable{

    //以下属性不与数据库交互，与es交互
    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    Long hotScore=0L;    //自定义一个字段，来保存热度评分

    List<SkuAttrValue_es> skuAttrValueList;
}
