package com.atguigu.gmall.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.service.ManageSkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;

/**
 * User: Administrator
 * Date:2018/4/14 0014
 */
@Service
public class ManageSkuServiceImpl implements ManageSkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = null;
        try {
            Jedis jedis = redisUtil.getJedis();
            String skuinfokey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            String skuinfoJson = jedis.get(skuinfokey);
            if (skuinfoJson==null || skuinfoJson.length()==0) {
                System.out.println(Thread.currentThread().getName() + "未命中！！");
                //加锁，防止直接并发连接数据库
                String skulockkey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String lock = jedis.set(skulockkey, "ok", "NX", "PX", RedisConst.SKUKEY_EXPIRE_PX);

                if ("OK".equals(lock)) {
                    System.out.println(Thread.currentThread().getName() + "获得锁！！");
                    skuInfo = getSkuInfoDB(skuId);

                    //防止连续查询数据库中不存在的值
                    if (skuInfo == null) {
                        jedis.setex(skuinfokey, RedisConst.SKUKEY_TIMEOUT, "empty");
                        jedis.close();
                        return null;
                    }

                    skuinfoJson = JSON.toJSONString(skuInfo);
                    jedis.setex(skuinfokey, RedisConst.SKUKEY_TIMEOUT, skuinfoJson);
                    jedis.close();
                    return skuInfo;
                } else {
                    System.out.println(Thread.currentThread().getName() + "未获得锁，自旋！！");
                    Thread.sleep(1000);

                    jedis.close();
                    return getSkuInfo(skuId);
                }
            }else if (skuinfoJson.equals("empty")){

                return null;
            }else {
                System.out.println(Thread.currentThread().getName()+"命中缓存！！");
                skuInfo=JSON.parseObject(skuinfoJson,SkuInfo.class);
                jedis.close();
                return skuInfo;
            }

        }catch (JedisConnectionException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }


        return getSkuInfoDB(skuId);

    }


    public SkuInfo getSkuInfoDB(String skuId){
        System.out.println(Thread.currentThread().getName()+"查询数据库！！");
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        if (skuInfo!=null){
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

            skuInfo.setSkuImageList(skuImageList);

        }

        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String id, String spuId) {

        List<SpuSaleAttr> spuSaleAttrListCheckBySku =
                spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(id),Long.parseLong(spuId));
        return spuSaleAttrListCheckBySku;
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueBySpu(String spuId) {

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu =
                skuSaleAttrValueMapper.selectSkuSaleAttrValueBySpu(Long.parseLong(spuId));
        return skuSaleAttrValueListBySpu;
    }
}
