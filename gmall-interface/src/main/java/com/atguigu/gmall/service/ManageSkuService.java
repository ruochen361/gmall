package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface ManageSkuService {

    public SkuInfo getSkuInfo(String skuId);

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String id, String spuId);

    public List<SkuSaleAttrValue> getSkuSaleAttrValueBySpu(String spuId);
}
