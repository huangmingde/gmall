package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UserInfo;

public interface User_Service {


    /**
     *  用户登录方法
     */
    UserInfo login(UserInfo userInfo);

    /**
     *  根据userId，看看缓存中是否有userInfo。有-->>已经登录
     */
    UserInfo verify_byRedis(String userId);
}
