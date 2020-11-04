package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 封装搜索结果集
 */
@Data
public class Sku_SearchResult implements Serializable{

    //  商品信息skuInfo
    List<SkuInfo_es> skuInfoList_es;

    //分页
    long total;
    long totalPages;

    //用于封装attrValueId，通过attrValueId，可以得到平台属性值
    List<String> attrValueIdList;
}
