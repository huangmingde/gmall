package com.atguigu.gmall.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.User_address;
import com.atguigu.gmall.service.User_address_service;
import com.atguigu.gmall.user.mapper.User_address_mapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

@Service
public class UserAddress_serviceImpl implements User_address_service {

    @Autowired
    User_address_mapper userAddressMapper;

    @Override
    public List<User_address> getUserAddressList_by_userId(String userId) {
        User_address userAddress = new User_address();
        userAddress.setUserId(userId);

        // 调用mapper
        // select * from userAddress where userId=?
        return this.userAddressMapper.select(userAddress);
    }
}
