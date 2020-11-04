package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseAttrInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //主键自增
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    /*
        临时字段。表示该字段不是表中的字段，是业务需要的（1：N），
        封装来自页面的数据（BaseAttrInfo和多个BaseAttrValue）
     */
    @Transient
    private List<BaseAttrValue> attrValueList;
}
