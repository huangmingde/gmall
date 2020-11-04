package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface Payment_Service {

    /**
     *  功能：保存交易记录
     */
    void savePaymentInfo(PaymentInfo paymentInfo);
}
