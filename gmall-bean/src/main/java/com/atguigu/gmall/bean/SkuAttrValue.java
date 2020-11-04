package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class SkuAttrValue implements Serializable{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @Id
    String id;

    @Column
    String attrId;

    @Column
    String valueId;

    @Column
    String skuId;
}
