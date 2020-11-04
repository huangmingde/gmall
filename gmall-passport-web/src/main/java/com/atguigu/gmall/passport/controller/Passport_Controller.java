package com.atguigu.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.utils.JwtUtil;
import com.atguigu.gmall.service.User_Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Passport_Controller {

    @Value("token.key")
    private String key;


    @Reference
    private User_Service userService;

    /**
     *登录页
     */
    @RequestMapping("/index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }


    /**
     *  用户登录请求
     */
    // 控制器获取页面的数据
    @RequestMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        // salt 服务器的IP 地址
        String salt = request.getHeader("X-forwarded-for");
        // 调用登录方法
        UserInfo info = userService.login(userInfo);
        if (info!=null){
            // 如果登录成之后,返回token!
            // 如何制作token!
            HashMap<String , Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            // 生成token
            String token = JwtUtil.encode(key, map, salt);
            return token;
        }else {
            return "fail";
        }
    }

    /**
     * 认证用户是否登录
     */
    // http://passport.atguigu.com/verify?token=xxx&salt=x
    @RequestMapping("/verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //1.获取服务器的Ip，token
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        //2.key+ip ,解密token 得到用户的信息map（userId,nickName）
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        //3.判断用户是否登录（根据userId，区redis查一查有没有这个用户）
        if (map!=null && map.size()>0){
            String userId = (String) map.get("userId");
            System.out.println("userId=="+userId);
            UserInfo userInfo = this.userService.verify_byRedis(userId);
            //4.userInfo!=null true success; false fail;
            if (userInfo!=null){
                return "success";   //已经登录
            }else {
                return "fail";      //没有登录
            }
        }

        return "fail";      //没有登录
    }

}
