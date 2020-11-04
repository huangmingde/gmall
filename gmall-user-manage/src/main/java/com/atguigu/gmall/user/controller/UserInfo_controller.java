package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfo_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfo_controller {

    @Autowired
    UserInfo_service userInfoService;

    @ResponseBody
    @RequestMapping("/selectAll")
    public List<UserInfo> selectAll(){
        return this.userInfoService.selectAll();
    }

}
