package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.ManageSkuService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * User: Administrator
 * Date:2018/4/13 0013
 */
@Controller
public class SkuManageController {

    @Reference
    ManageService manageService;

    @Reference
    ManageSkuService manageSkuService;


    @Reference
    ListService listService;

   /* @RequestMapping("getAttrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(@RequestParam String catalog3Id){

        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3Id);
        return attrInfoList;
    }*/

    @RequestMapping("getSpuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(@RequestParam String spuId){

        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }



  /*  @RequestMapping("getSpuImgList")
    @ResponseBody
    public List<SpuImage> getSpuImgList(@RequestParam String spuId){

        List<SpuImage> spuImageList = manageService.getSpuImgList(spuId);
        return spuImageList;
    }*/




    @RequestMapping(value = "saveSkuInfo",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        //商品上架，保存至elasticsearch
        onSale(skuInfo);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("onSale")
    @ResponseBody
    //sku上架，skuinfo保存至elasticsearch
    public void onSale(SkuInfo skuInfo){
        //SkuInfo skuInfo = manageSkuService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();

        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

       listService.saveSkuLsInfo(skuLsInfo);
    }

    @RequestMapping("getSkuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> getSkuInfoListBySpu(@RequestParam String spuId){
        List<SkuInfo> skuInfoList  = manageSkuService.getSkuInfoListBySpu(spuId);
        return skuInfoList;
    }


}
