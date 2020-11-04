package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValue_Mapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 根据spuId，获取skuSaleAttrValue集合
     */
    List<SkuSaleAttrValue> getskuSaleAttrValueList_BySpuId(@Param("spuId") String spuId);
}
