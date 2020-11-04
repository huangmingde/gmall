package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin        //  跨域：解决不同IP和端口
@RestController
public class Manage_Controller {

    @Reference      // dubbo：远程调用服务
    private Manage_Service manageService;

    /**
     *  查询数据（一级、二级、三级、平台属性）
     */
    @RequestMapping("/getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return this.manageService.getBaseCatalog1();
    }

    //  http://localhost:8082/getCatalog2?catalog1Id=6
    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return this.manageService.getBaseCatalog2(catalog1Id);
    }

    //  http://localhost:8082/getCatalog3?catalog2Id=33
    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return this.manageService.getBaseCatalog3(catalog2Id);
    }

    //  http://localhost:8082/attrInfoList?catalog3Id=285
    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){
        return this.manageService.getBaseAttrInfo_list(catalog3Id);
    }

    /**
     *  插入数据（平台属性、平台属性值）
     */
    @RequestMapping("/saveAttrInfo")
    public void saveAttrInfo_AttrValue(@RequestBody BaseAttrInfo baseAttrInfo){
                        //控制器方法参数接收页面数据BaseAttrInfo（包含多个BaseAttrValue）
        this.manageService.saveAttrInfo_AttrValue(baseAttrInfo);
    }

    /*
        根据平台属性id，查询平台属性值的集合
        点击修改，回显数据
        http://localhost:8082/getAttrValueList?attrId=121
     */
//    @RequestMapping("/getAttrValueList")
//    public List<BaseAttrValue> getAttrValueList(String attrId){
//        return this.manageService.getAttrValueList(attrId);
//    }


    //符合业务的正确做法
    @RequestMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        //先根据“attrId”，查询平台属性    select * from BaseAttrInfo where id = attrId
        BaseAttrInfo baseAttrInfo = this.manageService.getBaseAttrInfo_ById(attrId);
        //再从平台属性中，获取平台属性值集合（平台属性包含一个平台属性值的成员）
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();

        return baseAttrValueList;
    }


}
