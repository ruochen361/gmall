package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * User: ruochen
 * Date:2018/4/18 0018
 */
@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

/*
    @RequestMapping("list.html")
    public String searchList(SkuLsParams skuLsParams, Model model) {
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        model.addAttribute("skuLsInfoList", skuLsInfoList);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getBaseAttrInfoList(attrValueIdList);


        String urlParam = makeUrlParam(skuLsParams,"");
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("keyword", skuLsParams.getKeyword());

        List<BaseAttrValue> attrValueSelectList = new ArrayList<>();
        for (Iterator<BaseAttrInfo> baseAttrInfoIterator = baseAttrInfoList.iterator(); baseAttrInfoIterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = baseAttrInfoIterator.next();

            for (Iterator<BaseAttrValue> baseAttrValueIterator = baseAttrInfo.getAttrValueList().iterator(); baseAttrValueIterator.hasNext(); ) {
                BaseAttrValue baseAttrValue = baseAttrValueIterator.next();
                if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().size()>0){
                for (String valueId : skuLsParams.getValueId()) {
                    if (valueId.equals(baseAttrValue.getId())) {

                        BaseAttrValue attrValueSelect = new BaseAttrValue();
                        attrValueSelect.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                        attrValueSelect.setId(valueId);
                        attrValueSelect.setUrlParam(makeUrlParam(skuLsParams,valueId));
                        attrValueSelectList.add(attrValueSelect);
                        baseAttrInfoIterator.remove();
                    }

                }}
            }
        }
        model.addAttribute("attrValueSelectList",attrValueSelectList);
        model.addAttribute("baseAttrInfoList", baseAttrInfoList);

        //String skuLsResultJSON = JSON.toJSONString(skuLsResult);
        //System.out.println("skuLsResultJSON = " + skuLsResultJSON);
        return "list";
    }
*/


    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        List<String> paramList = new ArrayList<>();

        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            paramList.add("keyword=" + skuLsParams.getKeyword());
        }
        if (skuLsParams.getCatalog3Id() != null) {
            paramList.add("catalog3Id=" + skuLsParams.getCatalog3Id());
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().size() > 0) {
            for (String valueId : skuLsParams.getValueId()) {

                        if(excludeValueIds!=null&&excludeValueIds.length>0){
                            String excludeValueId = excludeValueIds[0];
                            if (excludeValueId.equals(valueId)){
                                continue;
                            }
                        }
                        paramList.add("valueId=" + valueId);
                    }
        }
        String urlParam = StringUtils.join(paramList, "&");
        return urlParam;

    }

    @RequestMapping("list.html")
    public String searchList(SkuLsParams skuLsParams, Model model) {
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        model.addAttribute("skuLsInfoList", skuLsInfoList);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getBaseAttrInfoList(attrValueIdList);


        Map<String,Object> map = makeUrlParams(skuLsParams,baseAttrInfoList);
        model.addAttribute("urlParam",map.get("urlParam"));
        model.addAttribute("attrValueSelectList",map.get("attrValueSelectList"));
        model.addAttribute("keyword", skuLsParams.getKeyword());

        model.addAttribute("baseAttrInfoList", baseAttrInfoList);
        //String skuLsResultJSON = JSON.toJSONString(skuLsResult);
        //System.out.println("skuLsResultJSON = " + skuLsResultJSON);
        return "list";
    }

    private Map<String,Object> makeUrlParams(SkuLsParams skuLsParams,List<BaseAttrInfo> baseAttrInfoList) {
        List<String> paramList = new ArrayList<>();
        List<String> paramList2 = new ArrayList<>();
        List<BaseAttrValue> attrValueSelectList = new ArrayList<>();
        Map<String,Object> map =new HashMap<>();
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            paramList.add("keyword=" + skuLsParams.getKeyword());
            paramList2.add("keyword=" + skuLsParams.getKeyword());

        }
        if (skuLsParams.getCatalog3Id() != null) {
            paramList.add("catalog3Id=" + skuLsParams.getCatalog3Id());
            paramList2.add("catalog3Id=" + skuLsParams.getCatalog3Id());
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().size() > 0) {
            for (String valueId : skuLsParams.getValueId()) {
                paramList.add("valueId=" + valueId);
                for (Iterator<BaseAttrInfo> baseAttrInfoIterator = baseAttrInfoList.iterator(); baseAttrInfoIterator.hasNext(); ) {
                    BaseAttrInfo baseAttrInfo = baseAttrInfoIterator.next();
                    for (Iterator<BaseAttrValue> baseAttrValueIterator = baseAttrInfo.getAttrValueList().iterator(); baseAttrValueIterator.hasNext(); ) {
                        BaseAttrValue baseAttrValue = baseAttrValueIterator.next();
                        if (valueId.equals(baseAttrValue.getId())) {
                            baseAttrInfoIterator.remove();

                            //面包屑
                            BaseAttrValue attrValueSelect = new BaseAttrValue();
                            attrValueSelect.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                            attrValueSelect.setId(valueId);
                            for (String valueId2 : skuLsParams.getValueId()) {
                                if (valueId2.equals(baseAttrValue.getId())) {
                                    continue;
                                }
                                paramList2.add("valueId=" + valueId2);
                            }
                            String urlParam2 = StringUtils.join(paramList2, "&");
                            attrValueSelect.setUrlParam(urlParam2);
                            attrValueSelectList.add(attrValueSelect);

                            String substringBefore = StringUtils.substringBefore(urlParam2, "&valueId");
                            paramList2.clear();
                            paramList2.add(substringBefore);
                        }
                    }
                }
            }
        }
        String urlParam = StringUtils.join(paramList, "&");
        map.put("urlParam",urlParam);
        map.put("attrValueSelectList",attrValueSelectList);
        return map;

    }


}
