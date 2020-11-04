package com.atguigu.gmall.payment.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfo_Mapper;
import com.atguigu.gmall.service.Payment_Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service    //暴露服务
public class Payment_ServiceImpl implements Payment_Service {

    @Autowired
    PaymentInfo_Mapper paymentInfoMapper;


    /**
     *  功能：保存交易记录
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfoMapper.insertSelective(paymentInfo);
    }
}
