package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface Cart_Service  {

    /**
     * 功能：添加商品到购物车
     * 参数分析：
     * userId：必须要有用户id，要知道哪个用户购买的商品
     * skuId、skuNum：根据控制器“/addToCart”接收的表单参数获得（得自己去页面里找）
     */
    void  addToCart(String userId,String skuId,Integer skuNum);


    //登录状态下查询购物车。从redis中取，或者从数据库中取
    List<CartInfo> getCartList(String userId);

    /**
     * 功能：合并购物车（cookie购物车和DB购物车）
     */
    List<CartInfo> mergeCart_CookieAndDB(List<CartInfo> cartInfoList_CK, String userId);

    //登录状态。功能：购物车勾选状态
    void checkCart(String skuId, String isChecked, String userId);

    //根据用户id，获取缓存中，被选中的购物车
    List<CartInfo> getCartListChecked_onRedis(String userId);

    //根据userId，查询用户的购物车数据｛含skuPrice实时价格｝,并放进缓存
    List<CartInfo> getCartInfoDB_toCache(String userId);
}
