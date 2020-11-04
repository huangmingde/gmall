package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class SpuSaleAttr implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrName;

    @Transient
    private List<SpuSaleAttrValue> spuSaleAttrValueList;
}
