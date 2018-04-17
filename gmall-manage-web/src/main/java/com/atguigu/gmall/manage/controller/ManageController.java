package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


/**
 * User: Administrator
 * Date:2018/4/9 0009
 */
@Controller
public class ManageController {

    @Reference
    ManageService manageService;

    @RequestMapping("index")
    public String index(){

        return "index";
    }

    @RequestMapping("attrListPage")
    public String attrListPage(){

        return "attrListPage";
    }

    /**
     * 获得一级分类
     * @return List<BaseCatalog1>
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){

        List<BaseCatalog1> catalog1List = manageService.getCatalog1();

        return catalog1List;
    }

    /**
     * 获得二级分类
     * @param map
     * @return List<BaseCatalog1>
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public  List<BaseCatalog2> getCatalog2(@RequestParam Map<String,String> map){
        String catalog1Id = map.get("catalog1Id");
        List<BaseCatalog2> catalog2List = manageService.getCatalog2(catalog1Id);

        return catalog2List;
    }

    /**
     * 获得三级分类
     * @param map
     * @return List<BaseCatalog3>
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public  List<BaseCatalog3> getCatalog3(@RequestParam Map<String,String> map){
        String catalog2Id = map.get("catalog2Id");
        List<BaseCatalog3> catalog3List = manageService.getCatalog3(catalog2Id);

        return catalog3List;
    }

    /**
     * 获得属性信息列表
     * @param map
     * @return List<BaseAttrInfo>
     */
    @RequestMapping("getAttrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(@RequestParam Map<String,String> map){
        String catalog3id = map.get("catalog3id");
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3id);

        return attrInfoList;
    }

    /**
     * 保存属性信息（添加、修改）
     * @param baseAttrInfo
     * @return String
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String  saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    /**
     * 获取属性值列表
     * @param map
     * @return List<BaseAttrValue>
     */
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(@RequestParam Map<String,String> map){
        String attrId = map.get("attrId");
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        return attrValueList;
    }


    /**
     * 删除属性信息
     * @param  baseAttrInfo
     * @return  String
     */
    @RequestMapping("deleteAttrInfo")
    @ResponseBody
    public  String deleteAttrInfo(BaseAttrInfo baseAttrInfo){

        manageService.deleteAttrInfo(baseAttrInfo);
        return "success";
    }



}
