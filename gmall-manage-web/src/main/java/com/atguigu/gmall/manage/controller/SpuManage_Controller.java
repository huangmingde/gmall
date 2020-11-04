package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class SpuManage_Controller {

    //引用服务serviceImpl
    @Reference
    private Manage_Service manageService;


    /*
        查询所有的SPU信息
        http://localhost:8082/spuList?catalog3Id=1
        根据三级id，查询所有SPU信息  select * from SpuInfo where catalog3Id = ？
        实体类对象封装:
                SpuInfo对象中有catalog3Id
     */
    @RequestMapping("/spuList")
    public List<SpuInfo> getSpuList(SpuInfo spuInfo){
        List<SpuInfo> spuInfoList  = manageService.getSpuList(spuInfo);
        return spuInfoList;
    }

    /**
     * 查询基本的销售属性 。
     * 控制器：http://localhost:8082/baseSaleAttrList
     */
    @RequestMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttr_List = this.manageService.getBaseSaleAttrList();
        return baseSaleAttr_List;
    }

    /**
     * 保存SpuInfo信息【SpuImage、SpuSaleAttr（SpuSaleAttrValue）】
     * 控制器：http://localhost:8082/saveSpuInfo
     */
    @RequestMapping("/saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        if (spuInfo!=null){
            this.manageService.saveSpuInfo(spuInfo);
        }
    }
}
