package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.List_Service;
import com.atguigu.gmall.service.Manage_Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class List_Controller {

    @Reference  //引用远程服务
    private List_Service listService;

    @Reference
    private Manage_Service manageService;

//    @ResponseBody
    @RequestMapping("/list.html")
    public String listSkuInfo_es(Sku_SearchParams skuSearchParams, HttpServletRequest request) throws IOException {
        //设置每页显示的数据条数。
        skuSearchParams.setPageSize(2);

        Sku_SearchResult skuSearchResult = this.listService.searchSkuInfo_fromES(skuSearchParams);

        /**
         * 功能：skuInfo_es 循环显示到页面
         */
        List<SkuInfo_es> skuInfoListEs = skuSearchResult.getSkuInfoList_es();


        /**
         * 功能：搜索结果页面，显示平台属性名称、平台属性名称。
         */
        List<String> attrValueIdList = skuSearchResult.getAttrValueIdList();
        //根据平台属性值id集合，获取BaseAttrInfoList（含平台属性名称、平台属性值名称）
        List<BaseAttrInfo> baseAttrInfo_list = this.manageService.getBaseAttrInfoList(attrValueIdList);


        /**
         * 功能：点击平台属性值后，筛选商品
         */
        //1、编写一个方法：制作url后面的参数
        String urlParam = makeUrlParam(skuSearchParams);
        //2、如果Sku_SearchParams.valueId（skuAttrValueId）==baseAttrInfo_list-BaseAttrInfo.attrValueList-BaseAttrValue.id，
        //则可以将baseAttrInfo_list中对应的数据删除。
        //遍历集合过程中，想删除数据，必须使用迭代器。快捷键itco

        /**
         * 功能：显示面包屑
         * 定义面包屑集合（因为面包屑有很多）
         */
        List<BaseAttrValue> baseAttrValueList_b = new ArrayList<>();
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfo_list.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if (skuSearchParams.getValueId()!=null && skuSearchParams.getValueId().length>0){
                    for (String valueId : skuSearchParams.getValueId()) {
                        //判断用户选择的平台属性值id==来自DB的平台属性值id？等于--->>删除
                        if (valueId.equals(baseAttrValue.getId())){
                            //如果平台属性值id相等，则移除baseAttrInfo
                            iterator.remove();
                            /**
                             * 制作面包屑：把平台属性值的valueName设置成面包屑,后添加到面包屑集合
                             * 面包屑（baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName()）
                             */
                            BaseAttrValue baseAttrValue_b = new BaseAttrValue();
                            baseAttrValue_b.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            /**
                             * 功能：X掉面包屑（传递当前valueId，重新拼接url后的搜索参数）
                             * valueId：因为重新请求当前控制器。所以它是utl上搜索参数的valueId。
                             */
                            String newUrlParam = makeUrlParam(skuSearchParams, valueId);
                            baseAttrValue_b.setUrlParam(newUrlParam);
                            baseAttrValueList_b.add(baseAttrValue_b);
                        }
                    }
                }
            }
        }

        /**
         * 功能：设置分页。
         */
        request.setAttribute("pageNo",skuSearchParams.getPageNo());
        request.setAttribute("totalPages", skuSearchResult.getTotalPages());

        //放到域中
        request.setAttribute("skuInfoListEs", skuInfoListEs);
        request.setAttribute("baseAttrInfo_list", baseAttrInfo_list);
        request.setAttribute("urlParam", urlParam);
        //把面包屑存到域中
        request.setAttribute("baseAttrValueList_b", baseAttrValueList_b);
        //保存一个检索关键字（面包屑处）
        request.setAttribute("keyword_b", skuSearchParams.getKeyword());


        return "list";
    }

    //编写一个方法：制作url后面的搜索参数
    //因为，valueId有很多个，因此使用可变参数
    private String makeUrlParam(Sku_SearchParams skuSearchParams,String ... valueIds_b) {
        String urlParam = "";

        //【http://list.gmall.com/list.html?keyword=手机&catalog3Id=61&valueId=80】
        //判断keyword（是否是搜索框输入关键字）
        if (skuSearchParams.getKeyword()!=null && skuSearchParams.getKeyword().length()>0){
            urlParam+="keyword="+skuSearchParams.getKeyword();
        }

        //判断catalog3Id（用户选择三级分类id作为搜索条件）
        if (skuSearchParams.getCatalog3Id()!=null && skuSearchParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuSearchParams.getCatalog3Id();
        }

        //判断valueId（用户选择多个平台属性值名称作为筛选条件）
        if (skuSearchParams.getValueId()!=null && skuSearchParams.getValueId().length>0){
            for (String valueId : skuSearchParams.getValueId()) {
                /**
                 * 功能：X掉面包屑
                 * （当前点击的面包屑的valueId==搜索参数的valueId，本次循环不再拼接参数）
                 */
                if (valueIds_b!=null && valueIds_b.length>0){
                    //获取当前点"X"的面包屑的valueId。只有一个，因此是0
                    String valueId_b_checked = valueIds_b[0];
                    if (valueId_b_checked.equals(valueId)){
                        // break、continue
                        continue;   //跳出本次循环，本次不再拼接valueId
                    }
                }

                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }

        /**
         * 将拼好的url参数返回
         * http://list.gmall.com/list.html?keyword=手机&valueId=80【关键字搜索-->>选择平台属性值】
         * http://list.gmall.com/list.html?catalog3Id=61&valueId=80【三级分类搜索-->>选择平台属性值】
         */
        return urlParam;
    }

}
