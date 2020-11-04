package com.atguigu.gmall.user.service.Impl;


import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfo_service;
import com.atguigu.gmall.user.mapper.UserInfo_mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfo_serviceImpl implements UserInfo_service {

    @Autowired
    UserInfo_mapper userInfoMapper;

    @Override
    public List<UserInfo> selectAll() {
        return this.userInfoMapper.selectAll();
    }
}
