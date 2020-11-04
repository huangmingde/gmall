package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.Cart_Service;
import com.atguigu.gmall.service.Manage_Service;
import com.atguigu.gmall.service.Order_Service;
import com.atguigu.gmall.service.User_address_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class Order_controller {

    @Reference      //引用远程服务“User_address_service”
    User_address_service userAddressService;

    @Reference
    Cart_Service cartService;

    @Reference
    Order_Service orderService;

    @Reference
    Manage_Service manageService;

    @LoginRequire   //去结算必须登录
    @RequestMapping("/trade")
    public String trade(HttpServletRequest request){
        //获取登录用户
        String userId = (String) request.getAttribute("userId");
        //根据userId，获取用户地址
        List<User_address> userAddressList = this.userAddressService.getUserAddressList_by_userId(userId);
        request.setAttribute("userAddressList",userAddressList);

        /**
         * 功能：展示送货清单
         * 送货清单数据来源--->>被选中的购物车
         */
        //1、根据用户id，获取缓存中被选中的购物车
        List<CartInfo> cartInfoList = this.cartService.getCartListChecked_onRedis(userId);
        //2、将购物车（购物车）赋值给OrderDetail，并添加到订单明细集合（List<OrderDetail>）
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //添加到集合
            orderDetailList.add(orderDetail);
        }
        //计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);  //计算总金额，需要设值OrderDetailList
        //调用计算总金额的方法
        orderInfo.sumTotalAmount();
        //获取总金额，放到域中
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        //把订单明细集合放到域中
        request.setAttribute("orderDetailList",orderDetailList);

        /**
         * 功能开发：防止表单重复提交
         */
        //生成流水号，放到域中（页面流水号）
        String tradeNo = this.orderService.getTradeNo(userId);
        request.setAttribute("tradeNo", tradeNo);

        return "trade";
    }


    /**
     *  功能：提交订单
     */
    @RequestMapping("/submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request,OrderInfo orderInfo){  //对象直接接受页面的OrderInfo
        String userId = (String) request.getAttribute("userId");
        //缺一个userId，手动设置
        orderInfo.setUserId(userId);

        /**
         * 功能开发：防止表单重复提交（保存订单之前校验是否重复提交）
         */
        //1、获取页面流水号
        String tradeNo = request.getParameter("tradeNo");
        //2、检验流水号
        boolean result = this.orderService.checkTradeCode(userId, tradeNo);
        if (!result){   //falseL不允许提交表单
            request.setAttribute("errMsg", "表单已提交，不能够重复提交表单");
            return "tradeFail";
        }

        //---功能--验证库存（验证库存放在保存订单之前，有库存才保存订单）
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //  调用校验库存的方法：/hasStock?skuId=XXX&num=xxxx
            boolean flag = this.orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!flag){
                request.setAttribute("errMsg", orderDetail.getSkuName()+"库存不足");
                return "tradeFail";
            }
            //----功能--校验价格
            SkuInfo skuInfo = this.manageService.getSkuInfo_bySkuId(orderDetail.getSkuId());
            int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
            if (res!=0){    //库存价格和订单价格不相等
                request.setAttribute("errMsg", orderDetail.getSkuName()+"库存价格与订单价格不匹配");
                //更新订单的价格！并放进缓存
                this.cartService.getCartInfoDB_toCache(userId);
                return "tradeFail";
            }
        }

        //调用订单服务，保存OrderInfo（OrderDetail）
        String orderId = this.orderService.saveOrderInfo(orderInfo);

        //3、删除流水号
        this.orderService.delTradeCode(userId);

        //转发到支付页面
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }


    //    http://localhost:8081/userAddres?userId=1
    @RequestMapping("/userAddres")
    @ResponseBody
    public List<User_address> userAddresList(String userId){

        return this.userAddressService.getUserAddressList_by_userId(userId);
    }
}
