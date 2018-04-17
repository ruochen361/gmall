package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * User: Administrator
 * Date:2018/4/13 0013
 */
@Controller
public class SkuManageController {

    @Reference
    ManageService manageService;

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


        return ResponseEntity.ok().build();
    }


}
