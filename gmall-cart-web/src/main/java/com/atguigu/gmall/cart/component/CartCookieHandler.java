package com.atguigu.gmall.cart.component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.Manage_Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference  //远程调用服务
    private Manage_Service manageService;


    // 未登录的时候，添加到购物车
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String userId, String skuId, Integer skuNum){
        //声明一个集合：存储不同的商品。
        List<CartInfo> cartInfoList = new ArrayList<>();
        //借助一个boolean类型的变量，确定是否往集合添加商品。true，则添加新商品到集合；false，不添加。
        boolean ifExist=false;  //默认false

        //从cookie中，获取购物车数据（购物车中有可能有中文，因此要设置编码）
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        //字符串cookieValue不能为空。
        if (StringUtils.isNotEmpty(cookieValue)){
            //cookieValue包含多个cartInfo实体类【List<CartInfo>】
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            if (cartInfoList!=null && cartInfoList.size()>0){
                for (CartInfo cartInfo : cartInfoList) {      //
                    //判断商品是否相同。
                    if (cartInfo.getSkuId().equals(skuId)){
                        //相同---->>相加
                        cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                        //初始化实时价格（skuPrice=CartPrice）
                        cartInfo.setSkuPrice(cartInfo.getCartPrice());
                        //商品相同---->>更改为true
                        ifExist = true;   //走完一遍流程
                    }
                }
            }
        }
        //true。表示for(if)过后，没有该商品--->>新存储到集合中。
        if (!ifExist){
            //获取到商品详情页所添加的不同商品（skuInfo）
            SkuInfo skuInfo = this.manageService.getSkuInfo_bySkuId(skuId);
            //把skuInfo的属性赋值给CartInfo
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);

            cartInfoList.add(cartInfo);
        }
        //将最终的集合放进cookie中
        CookieUtil.setCookie(request,response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }

    //未登录展示购物车数据，从cookie中取购物车数据
    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartInfoList_JSON = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = null;
        if (StringUtils.isNotEmpty(cartInfoList_JSON)){
            cartInfoList = JSON.parseArray(cartInfoList_JSON, CartInfo.class);
            return cartInfoList;
        }
        return cartInfoList;
    }

    //合并购物车后，删除cookie购物车数据
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    //未登录状态。功能：购物车勾选状态
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //思路：直接页面传递的"isChecked"，赋值给cookie中的"CartInfo"
        //查询cookie中的CartInfo集合
        List<CartInfo> cartInfoList = getCartList(request);
        if (cartInfoList!=null && cartInfoList.size()>0){
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){ //判断：确定是哪一个商品
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }
        //更新cookie
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
    }
}
