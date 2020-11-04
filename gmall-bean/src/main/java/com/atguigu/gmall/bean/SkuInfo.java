package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;

    @Column
    String spuId;

    @Column
    BigDecimal price;

    @Column
    String skuName;

    @Column
    BigDecimal weight;

    @Column
    String skuDesc;

    @Column
    String catalog3Id;

    @Column
    String skuDefaultImg;

    @Transient      //SkuInfo包含多张Sku图片
    List<SkuImage> skuImageList;

    @Transient      //SkuInfo包含多个平台属性（含平台属性值）
    List<SkuAttrValue> skuAttrValueList;

    @Transient      //SkuInfo包含多个销售属性（含销售属性值）
    List<SkuSaleAttrValue> skuSaleAttrValueList;
}

