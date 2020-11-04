package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.component.CartCookieHandler;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.Cart_Service;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class Cart_Controller {

    @Reference      //远程调用服务
    private Cart_Service cartService;

    @Autowired      //注入组件
    private CartCookieHandler cartCookieHandler;

    @Reference      //调用远程服务
    private Manage_Service manageService;

    //功能：添加到购物车
    @LoginRequire(autoRedirect = false) //作用：一、false表示添加到购物车不需要登录；二、可以得到登录用户的“userId”
    @RequestMapping("/addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //点击“加入购物”按钮，表单传递过来的两个参数
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");

        //获取登录拦截器（Auth_interceptor）在request域中存的“userId”
        String userId = (String) request.getAttribute("userId");
        //判断用户是否登录
        if (userId!=null){
            //用户已登录，调用登录时添加购物车的方法
            this.cartService.addToCart(userId, skuId, Integer.parseInt(skuNum));
        }else {
            //用户未登录，调用未登录时添加购物车的方法
            this.cartCookieHandler.addToCart(request, response, userId, skuId, Integer.parseInt(skuNum));
        }

        //页面要显示skuInfo信息
        //根据商品详情页面传递过来的skuId，得到skuInfo，并保存到域中
        SkuInfo skuInfo = this.manageService.getSkuInfo_bySkuId(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);//加入购物车的商品数量（来自商品详情页面）
        return "success";
    }

    /**
     *功能：展示购物车列表
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        List<CartInfo> cartInfoList = null;

        // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            /**
             * 功能：合并购物车（cookie购物车和DB购物车）
             */
            //获取cookie购物车数据
            List<CartInfo> cartInfoList_CK = this.cartCookieHandler.getCartList(request);
            if (cartInfoList_CK!=null && cartInfoList_CK.size()>0){
                //cookie购物车数据不为空，合并【cookie购物车数据+DB购物车数据】
                cartInfoList = this.cartService.mergeCart_CookieAndDB(cartInfoList_CK,userId);
                //合并购物车后，删除cookie购物车数据
                this.cartCookieHandler.deleteCartCookie(request,response);
            }else {
                //登录状态下查询购物车。【从redis中取，或者从数据库中取（根据userId，得到购物车信息）】
                cartInfoList = this.cartService.getCartList(userId); //因为登录状态，所以传"userId"
            }
        }else {
            //未登录状态下查询购物车，从cookie中取
            cartInfoList = this.cartCookieHandler.getCartList(request);
        }

        //保存数据到域
        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }

    /**
     *功能：购物车勾选状态
     */
    @ResponseBody
    @LoginRequire(autoRedirect = false) //表示不需要登录也可以访问该控制器。（应用于：可登录，也可不用登录的情况）
    @RequestMapping("/checkCart")
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");

        if (userId!=null){
            //登录状态。功能：购物车勾选状态
            this.cartService.checkCart(skuId,isChecked,userId);
        }else {
            //未登录状态。功能：购物车勾选状态
            this.cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    /**
     *  功能：点击“去结算”按钮，跳转结算页面
     */
    @LoginRequire   //结算：必须登录
    @RequestMapping("/toTrade")
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //合并勾选的商品（未登录+登录）
        //1、获取未登录商品
        List<CartInfo> cartInfoList_CK = this.cartCookieHandler.getCartList(request);
        String userId = (String) request.getAttribute("userId");
        if (cartInfoList_CK!=null && cartInfoList_CK.size()>0){
            //2、合并（去结算时，合并勾选状态。以cookie为基准）
            this.cartService.mergeCart_CookieAndDB(cartInfoList_CK, userId);
            //3、合并后，删除cookie数据
            this.cartCookieHandler.deleteCartCookie(request,response);
        }

        //跳转结算页面
        return "redirect://order.gmall.com/trade";
    }

}
