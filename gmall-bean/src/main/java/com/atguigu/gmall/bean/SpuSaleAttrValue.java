package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class SpuSaleAttrValue implements Serializable{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrValueName;

    @Transient
    private String isChecked;  //isChecked什么用?当前的属性值是否被选中!
}
