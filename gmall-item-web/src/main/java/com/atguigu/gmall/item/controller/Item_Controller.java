package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.List_Service;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class Item_Controller {

    @Reference  //远程调用服务
    private Manage_Service manageService;

    @Reference
    private List_Service listService;

    @LoginRequire   //表示访问该控制器需要登录
    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId, HttpServletRequest request) throws IOException {
        /**
         * 功能：显示SkuInf信息
         */
        //根据skuId，获取商品详情信息（SkuInfo），并添加到作用域
        SkuInfo skuInfo =  this.manageService.getSkuInfo_bySkuId(skuId);
        request.setAttribute("skuInfo",skuInfo);

        /**
         * 功能：显示SkuImage图片列表
         */
        //根据skuId，获取图片集合（SkuImageList），并添加到作用域
        List<SkuImage> skuImageList =  this.manageService.getSkuImageList_bySkuId(skuId);
        request.setAttribute("skuImageList",skuImageList);

        /**
         * 功能：1显示商品销售属性、商品销售属性值;2、根据skuId，锁定商品属性值。
         * 方法参数是skuInfo，为什么？因为skuInfo包含skuId（skuInfo.getId）、spuId（skuInfo.getSpuId）
         * 方法返回值是List<SpuSaleAttr>，为什么？因为SpuSaleAttr包含多个SpuSaleAttrValue（含isChecked）
         */
        List<SpuSaleAttr> spuSaleAttrList = this.manageService.getSpuSaleAttrList_Value_checked(skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);


        /**
         * 功能：用户选择不同的商品属性值组合，而跳转不同的skuId页面。
         * 例如：https://item.jd.com/{skuId}.html
         */
        //根据spuId，获取skuSaleAttrValue集合
        List<SkuSaleAttrValue> skuSaleAttrValueList = this.manageService.getskuSaleAttrValueList_BySpuId(skuInfo.getSpuId());
        //拼接字符串。
        //拼接key（key="138|140"），放入map集合【map.put(key,skuId)】，清空key（key=""）。
                    /*
                    实例：{"138|140":"44","139|140":"45"}
                    第1次拼串：key=138
                    第2次拼串：key=138|
                    第3次拼串：key=138|140 --->>放入map，清空key（key=""）。
                    第4次拼串：key=139
                    */
        String key = "";
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            //什么时候加“|”? 当字符串“key”长度大于0的时候
            //什么时候停止拼串（就是什么时候放入map，并清空key）？
            // 1、拼接完集合的最后，不拼接
            // 2、本次循环得到的skuId与下一次循环得到的skuId不相等的时候，不拼接
            if (key.length()>0){
                key+="|";
            }
            key += skuSaleAttrValue.getSaleAttrValueId();
            if ((i+1)==skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals( skuSaleAttrValueList.get(i+1).getSkuId() )){
                //放入map
                map.put(key,skuSaleAttrValue.getSkuId());
                //清空key
                key="";
            }
        }

        //map转成JSON。
        String json_savId = JSON.toJSONString(map);
        //JSON数据存入域中
        request.setAttribute("json_savId",json_savId);

//        System.out.println("拼好的串"+json_savId);

        /**
         * 访问“/{skuId}.html”，就会记录每个商品的详情页被用户访问的次数
         */
        listService.incrHotScore(skuId);

        //跳转到商品详情页（item.html）；
        return "item";
    }
}
