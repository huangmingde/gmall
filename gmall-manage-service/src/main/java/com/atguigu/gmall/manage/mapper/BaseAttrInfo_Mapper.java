package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfo_Mapper extends Mapper<BaseAttrInfo> {
    List<BaseAttrInfo> getBaseAttrInfoList_ByCatalog3Id(@Param("catalog3Id") String catalog3Id);


    /**
     * 根据平台属性值id集合，获取BaseAttrInfoList（含平台属性名称、平台属性值名称）
     */
    List<BaseAttrInfo> getBaseAttrInfoList(@Param("attrValueIds") String attrValueIds);
}
