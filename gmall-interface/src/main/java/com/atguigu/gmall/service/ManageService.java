package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public BaseAttrInfo getAttrInfo(String attrId);

    public void deleteAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    public List<BaseSaleAttr> getbaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuSaleAttr> getSpuSaleAttr(String spuId);

    public List<SpuImage> getSpuImageList(String spuId);

    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public List<SpuImage> getSpuImgList(String spuId);

    public void saveSkuInfo(SkuInfo skuInfo);

    public List<BaseAttrInfo> getBaseAttrInfoList(List<String> attrValueIdList);

}
