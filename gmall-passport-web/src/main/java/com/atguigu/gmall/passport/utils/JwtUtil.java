package com.atguigu.gmall.passport.utils;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {
    /**
     * 作用：生成TOKEN
     * @param key 公共部分
     * @param param 私有部分
     * @param salt 签名部分
     * @return
     */
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);
        // 将用户信息放入jwtBuilder
        jwtBuilder = jwtBuilder.setClaims(param);
        // 生成token
        String token = jwtBuilder.compact();
        return token;

    }

    /**
     * 作用：解析token，获得私有部分
     * @param token 生成的字符串token
     * @param key 公共部分
     * @param salt 盐值
     * @return
     */
    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
