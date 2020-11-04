package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;
//import com.sun.org.apache.xml.internal.resolver.Catalog;

import java.util.List;

public interface Manage_Service {

    // 查询所有一级分类数据
    List<BaseCatalog1> getBaseCatalog1();

    // 根据一级分类id，查询所有二级分类数据
    //select * from BaseCatalog2 where catalog1_id = ?
    List<BaseCatalog2> getBaseCatalog2(String catalog1Id);

    // 根据二级分类id，查询所有三级分类数据
    List<BaseCatalog3> getBaseCatalog3(String catalog2Id);

    // 根据三级分类id，查询平台属性集合
    List<BaseAttrInfo> getBaseAttrInfo_list(String catalog3Id);

    //  插入平台属性BaseAttrInfo（包含平台属性值BaseAttrValue）
    void saveAttrInfo_AttrValue(BaseAttrInfo baseAttrInfo);


    //  根据平台属性id，查询平台属性值的集合（点击修改平台属性后，回显平台属性值）
    List<BaseAttrValue> getAttrValueList(String attrId);


    //
    BaseAttrInfo getBaseAttrInfo_ById(String attrId);


    // =========  SPU =========================

    //根据三级分类id，查询SPU集合
    //List<SpuInfo> getSpuList(String catalog3Id);   //也可以这么写
    List<SpuInfo> getSpuList(SpuInfo spuInfo);      // 这么写更好（直接传一个对象，对象包含了“catalog3Id”）


    /**
     * 查询基本的销售属性 。
     * 控制器：http://localhost:8082/baseSaleAttrList
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存SpuInfo信息【SpuImage、SpuSaleAttr（SpuSaleAttrValue）】
     */
    void saveSpuInfo(SpuInfo spuInfo);


    /**
     *  加载SPU图片
     *  控制器：http://localhost:8082/spuImageList?spuId=58
     */
    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 功能：加载销售属性和销售属性值
     * 控制器：http://localhost:8082/spuSaleAttrList?spuId=76
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);


    /**
     * 保存SkuInfo【SkuAttrValue、SkuSaleAttrValue、SkuImage】
     * 控制器：http://localhost:8082/saveSkuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);


    /**
     * 根据skuId，获取商品详情信息（SkuInfo）
     */
    SkuInfo getSkuInfo_bySkuId(String skuId);

    /**
     * 功能：显示SkuImage图片列表
     */
    //根据skuId，获取图片集合（SkuImageList），并添加到作用域
    List<SkuImage> getSkuImageList_bySkuId(String skuId);

    /**
     * 功能：1显示商品销售属性、商品销售属性值;2、根据skuId，锁定商品属性值。
     * 方法参数是skuInfo，为什么？因为skuInfo包含skuId（skuInfo.getId）、spuId（skuInfo.getSpuId）
     * 方法返回值是List<SpuSaleAttr>，为什么？因为SpuSaleAttr包含多个SpuSaleAttrValue（含isChecked）
     */
    List<SpuSaleAttr> getSpuSaleAttrList_Value_checked(SkuInfo skuInfo);

    /**
     * 功能：用户选择不同的商品属性值组合，而跳转不同的skuId页面。
     * 例如：https://item.jd.com/{skuId}.html
     */
    //根据spuId，获取skuSaleAttrValue集合
    List<SkuSaleAttrValue> getskuSaleAttrValueList_BySpuId(String spuId);

    /**
     * 根据平台属性值id集合，获取BaseAttrInfoList（含平台属性名称、平台属性值名称）
     */
    List<BaseAttrInfo> getBaseAttrInfoList(List<String> attrValueIdList);


}
