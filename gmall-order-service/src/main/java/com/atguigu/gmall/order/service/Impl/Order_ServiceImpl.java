package com.atguigu.gmall.order.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderDetail_Mapper;
import com.atguigu.gmall.order.mapper.OrderInfo_Mapper;
import com.atguigu.gmall.service.Order_Service;
import com.atguigu.gmall.utils.HttpClientUtil;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service    //暴露服务
public class Order_ServiceImpl implements Order_Service {

    @Autowired
    OrderInfo_Mapper orderInfoMapper;

    @Autowired
    OrderDetail_Mapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    //功能：保存OrderInfo（OrderDetail）
    @Transactional  //开启事务控制
    @Override
    public String saveOrderInfo(OrderInfo orderInfo) {
        //页面传递的OrderInfo数据不完整，缺了（总金额，订单状态，第三方交易编号，创建时间，过期时间，进程状态）
        //总金额
        orderInfo.sumTotalAmount(); //页面数据直接封装，已经有数据了。不需要设值OrderDetailList
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //第三方交易编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);     //表示：当前日期增加一天
        orderInfo.setExpireTime(calendar.getTime());
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //保存OrderInfo（OrderDetail）
        this.orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //两表插入。内表某id=外表主键
            orderDetail.setOrderId(orderInfo.getId());
            this.orderDetailMapper.insertSelective(orderDetail);
        }
        //返回订单id
        return orderInfo.getId();
    }

    /**
     * 功能：生成流水号
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        //定义一个流水号
        String tradeCode = UUID.randomUUID().toString();
        //把流水号放缓存
        jedis.setex(tradeNoKey, 60*10,tradeCode);
        jedis.close();
        //返回流水号
        return tradeCode;
    }

    /**
     *  功能：验证流水号
     */
    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        //获取缓存的流水号
        String tradeNo = jedis.get(tradeNoKey);
        jedis.close();
        //页面上保存的流水号==缓存的流水号
        return tradeCodeNo.equals(tradeNo);
    }

    /**
     *  功能：删除缓存中的流水号
     */
    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    //验证库存
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        //远程调用控制器：http://www.gware.com/hasStock?skuId=XXXX&num=xxxx
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);  //返回"1"：表示有库存
    }

    //根据orderId，获得OrderInfo
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        //sql：select * from OrderInfo where id = orderId
        OrderInfo orderInfo = this.orderInfoMapper.selectByPrimaryKey(orderId);
        return orderInfo;
    }

}
