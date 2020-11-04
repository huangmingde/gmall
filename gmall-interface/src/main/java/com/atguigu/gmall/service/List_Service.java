package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo_es;
import com.atguigu.gmall.bean.Sku_SearchParams;
import com.atguigu.gmall.bean.Sku_SearchResult;

import java.io.IOException;
import java.util.List;

public interface List_Service {

    /**
     * 功能：保存skuInfo到es
     */
    void saveSkuInfo_es(SkuInfo_es skuInfoEs) throws IOException;

    /**
     * 功能：搜索skuInfo
     */
    Sku_SearchResult searchSkuInfo_fromES(Sku_SearchParams skuSearchParams) throws IOException;

    /**
     * 记录每个商品的详情页被用户访问的次数
     */
    void incrHotScore(String skuId) throws IOException;

}
