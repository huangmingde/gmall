package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.Order_Service;
import com.atguigu.gmall.service.Payment_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import static com.alipay.api.AlipayConstants.APP_ID;
import static com.alipay.api.AlipayConstants.SIGN_TYPE;

@Controller
public class Payment_Controller {

    @Reference    //调用远程服务
    Order_Service orderService;

    @Reference  //调用远程服务
    Payment_Service paymentService;

    @Autowired  //注入alipay（采用软编码的形式）
    AlipayClient alipayClient;


    @RequestMapping("/index")
    public String payment(HttpServletRequest request){

        //1、 获取订单id。 http://payment.gmall.com/index?orderId=98
        String orderId = request.getParameter("orderId");
        //2、 根据orderId，获得OrderInfo，在获得总金额totalAmount
        OrderInfo orderInfo = this.orderService.getOrderInfo(orderId);
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        //3、 将订单id、总金额保存到域中
        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", totalAmount);

        return "index";
    }


    /*
    1.  保存支付记录下 将数据放入数据库（paymentInfo表）
        去重复，对账！ 幂等性=保证每一笔交易只能交易一次 {第三方交易编号outTradeNo}！
    2.  生成二维码
    */
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        /**
         * 功能：保存支付数据
         */
        // 获取orderId
        String orderId = request.getParameter("orderId");
        // 通过orderId 将数据查询出来
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        // paymentInfo 数据来源于谁！orderInfo
        // 属性赋值！
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("hmd想买手机！");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        //  保存支付数据（paymentInfo）
        paymentService.savePaymentInfo(paymentInfo);

        /**
         * 功能：生成二维码！
         */
        // 参数做成配置文件，进行软编码！
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        // 设置同步回调
        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        // 设置异步回调
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        // 声明一个map 集合来存储参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        // 将封装好的参数传递给支付宝！
        alipayRequest.setBizContent(JSON.toJSONString(map));

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(form);//直接将完整的表单html输出到页面
//        response.getWriter().flush();
//        response.getWriter().close();
        return form;
    }

}
