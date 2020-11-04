package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttr_Mapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrList(@Param("spuId") String spuId);

    /**
     * 查询商品销售属性集合【商品销售属性值集合（isChecked）】
     */
    List<SpuSaleAttr> getSpuSaleAttrList_Value_checked(@Param("skuId")String skuId, @Param("spuId")String spuId);
}
