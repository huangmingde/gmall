package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfo_Mapper extends Mapper<CartInfo> {
    /**
     *  根据userId，获取购物车信息（CartList），并且含skuName
     *
     */
    List<CartInfo> getCartListWithSkuPrice(@Param("userId") String userId);
}
