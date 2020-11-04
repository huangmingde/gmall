package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装搜索参数
 */
@Data
public class Sku_SearchParams implements Serializable {

    String  keyword;    //keyword=skuName

    String catalog3Id;

    String[] valueId;   //valueId = skuAttrValueList.valueId

    int pageNo=1;

    int pageSize=20;
}
