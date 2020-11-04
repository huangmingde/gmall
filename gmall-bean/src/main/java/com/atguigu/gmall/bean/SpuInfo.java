package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class SpuInfo implements Serializable{

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuName;

    @Column
    private String description;

    @Column
    private  String catalog3Id;

    @Transient  //一个商品有多张图片，1：N
    private List<SpuSaleAttr> spuSaleAttrList;

    @Transient  //一个商品有多张图片，1：N
    private List<SpuImage> spuImageList;

}
