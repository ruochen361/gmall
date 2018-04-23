package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.ManageSkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Administrator
 * Date:2018/4/14 0014
 */
@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @Reference
    ManageSkuService manageSkuService;

    @Reference
    ListService listService;

    @RequestMapping("{skuId}.html")
    @LoginRequire
    public String getSkuInfo(@PathVariable("skuId")String skuId, Model model){

       SkuInfo skuInfo = manageSkuService.getSkuInfo(skuId);
       model.addAttribute("skuInfo",skuInfo);

       List<SpuSaleAttr> spuSaleAttrListCheckBySku =
               manageSkuService.getSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
       model.addAttribute("spuSaleAttrListCheckBySku",spuSaleAttrListCheckBySku);

       List<SkuSaleAttrValue> skuSaleAttrValueBySpuList =
               manageSkuService.getSkuSaleAttrValueBySpu(skuInfo.getSpuId());

        String saleValueIds = "";
        Map<String,String> saleValueSkuIdMap = new HashMap<>();

        for (int i=0;i<skuSaleAttrValueBySpuList.size();i++){

            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueBySpuList.get(i);
            if (saleValueIds.length()!=0){
                saleValueIds +="|";
            }
            saleValueIds += skuSaleAttrValue.getSaleAttrValueId();

            if (skuSaleAttrValueBySpuList.size()==i+1 ||
                    !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueBySpuList.get(i+1).getSkuId())) {
                saleValueSkuIdMap.put(saleValueIds, skuSaleAttrValue.getSkuId());
                saleValueIds = "";
            }
        }

        String saleValueSkuIdJSON = JSON.toJSONString(saleValueSkuIdMap);

       model.addAttribute("saleValueSkuIdJSON",saleValueSkuIdJSON);

       //增加点击热度评分，为查询列表排序
       listService.incrHotScore(skuId);

       return "item";
    }


}
