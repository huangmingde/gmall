package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.User_address;

import java.util.List;

public interface User_address_service {

    /*
    根据userId，查询用户地址列表
     */
    List<User_address> getUserAddressList_by_userId(String userId);

}
