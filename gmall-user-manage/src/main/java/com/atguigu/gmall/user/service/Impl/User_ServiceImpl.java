package com.atguigu.gmall.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.User_Service;
import com.atguigu.gmall.user.mapper.UserInfo_mapper;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

@Service
public class User_ServiceImpl implements User_Service {

    @Autowired
    private UserInfo_mapper userInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    /**
     *  功能：用户登录方法
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //sql：select * from UserInfo where loginName=? and passwd=?
        //一、根据用户名、密码，查询是否有该用户
        //对密码进行加密
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //使用加密后的密码，查询是否有该用户
        userInfo.setPasswd(newPasswd);
        UserInfo info = this.userInfoMapper.selectOne(userInfo);

        //二、查得到--->>把该用户放进redis缓存（方便其他模块获取），并返回
        if (info!=null){
            Jedis jedis = redisUtil.getJedis();
            // 制作key 【user:userId:info】
            String userKey = userKey_prefix+info.getId()+userKey_suffix;
            // 放缓存。【因为其他模块只查看，不修改，所以使用string并加上超时时间】
            jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;    //返回
        }
        return null;    //查不到
    }

    /**
     *  根据userId，看看缓存中是否有userInfo。有-->>已经登录
     */
    @Override
    public UserInfo verify_byRedis(String userId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            //制作key 【user:userId:info】
            String userKey = userKey_prefix+userId+userKey_suffix;
            //根据key，获取缓存中的userInfo
            String userInfo_JSON = jedis.get(userKey);
            if (!StringUtils.isEmpty(userInfo_JSON)){
                //  redis缓存中有数据，返回userInfo
                UserInfo userInfo = JSON.parseObject(userInfo_JSON, UserInfo.class);
                return userInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        //没有缓存
        return null;
    }

}
