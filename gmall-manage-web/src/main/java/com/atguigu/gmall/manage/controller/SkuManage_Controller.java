package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuInfo_es;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.List_Service;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class SkuManage_Controller {

    @Reference
    private Manage_Service manageService;

    @Reference
    private List_Service listService;

    /**
     *  加载SPU图片的控制器
     *  http://localhost:8082/spuImageList?spuId=58
     */
    @RequestMapping("/spuImageList")
    public List<SpuImage> getSpuImageList(SpuImage spuImage){ //SpuImage对象包含spuId
        List<SpuImage> spuImage_list = this.manageService.getSpuImageList(spuImage);
        return spuImage_list;
    }

    /**
     * 功能：加载销售属性和销售属性值
     * 控制器：http://localhost:8082/spuSaleAttrList?spuId=76
     */
    @RequestMapping("/spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return this.manageService.getSpuSaleAttrList(spuId);
    }

    /**
     * 保存SkuInfo【SkuAttrValue、SkuSaleAttrValue、SkuImage】
     * 控制器：http://localhost:8082/saveSkuInfo
     */
    @RequestMapping("/saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        if (skuInfo != null){
            this.manageService.saveSkuInfo(skuInfo);
        }
    }

    /**
     * 功能：上架商品（实际就是保存数据到es）
     */
    @RequestMapping("/onSale")
    public void onSale(String skuId) throws IOException {
        SkuInfo_es skuInfoEs = new SkuInfo_es();
        //给SkuInfo_es赋值
        SkuInfo skuInfo = this.manageService.getSkuInfo_bySkuId(skuId);
        //属性拷贝【skuInfo--->>skuInfoEs】
        BeanUtils.copyProperties(skuInfo,skuInfoEs);
        this.listService.saveSkuInfo_es(skuInfoEs);
    }
}

