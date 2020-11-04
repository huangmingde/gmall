package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface Order_Service {

    //功能：保存OrderInfo（OrderDetail）
    String saveOrderInfo(OrderInfo orderInfo);


    /**
     * 功能：生成流水号
     */
    String getTradeNo(String userId);

    /**
     *  功能：验证流水号
     * @param userId 获取缓存的流水号
     * @param tradeCodeNo 页面的流水号
     * @return  true：可以提交，false：不可以提交
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     *  功能：删除缓存中的流水号
     * @param userId 缓存中的流水号
     */
    void  delTradeCode(String userId);

    //验证库存
    boolean checkStock(String skuId, Integer skuNum);

    //根据orderId，获得OrderInfo
    OrderInfo getOrderInfo(String orderId);
}
