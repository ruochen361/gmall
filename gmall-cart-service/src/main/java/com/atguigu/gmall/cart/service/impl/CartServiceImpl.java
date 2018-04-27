package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageSkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ruochen
 * Date:2018/4/24 0024
 */
@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageSkuService manageSkuService;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public void addToCart(String userId, String skuNum, SkuInfo skuInfo) {

        //更新数据库
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuInfo.getId());
        cartInfo.setUserId(userId);
        CartInfo cartInfoExsit = cartInfoMapper.selectOne(cartInfo);
        //判断数据库的购物车中是否有该用户，该sku商品
        if (cartInfoExsit == null) {
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuNum(Integer.parseInt(skuNum));

            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExsit=cartInfo;
        }else {
            //存在就只进行更新
            cartInfoExsit.setSkuNum(cartInfoExsit.getSkuNum()+Integer.parseInt(skuNum));
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExsit);

        }
        //更新缓存
        addToCart(userId,cartInfoExsit);

    }


    //加入缓存
    private void addToCart(String userId,CartInfo cartInfo){
        //更新数据 hash
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartInfoJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,cartInfo.getSkuId(),cartInfoJson);

        //更新过期时间,与用户登录过期时间一致
        String userInfoKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_INFO_KEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }


    @Override
    public List<CartInfo> getCartList(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartInfoJsonList = jedis.hvals(userCartKey);

        List<CartInfo> cartInfoList = new ArrayList<>();

        //判断缓存中是否存在
        if (cartInfoJsonList != null&&cartInfoJsonList.size()>0) {

            for (String cartInfoJson : cartInfoJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //排序
            cartInfoList.sort((o1, o2)->{return Long.compare(Long.parseLong(o2.getId()) ,Long.parseLong(o1.getId()));});
        }else {

            //查询数据库,更新缓存
             cartInfoList = loadCartCache(userId);
        }
        jedis.close();
        return cartInfoList;
    }

    //更新缓存
    public  List<CartInfo> loadCartCache(String userId){
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartInfoListWithPrice(Long.parseLong(userId));
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        Map<String,String> cartMap = new HashMap<>(cartInfoList.size());

        for (CartInfo cartInfo : cartInfoList) {
            String cartInfoJson = JSON.toJSONString(cartInfo);
            cartMap.put(cartInfo.getSkuId(),cartInfoJson);
        }

        jedis.hmset(userCartKey, cartMap);
        jedis.close();
        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeCartList(List<CartInfo> cartInfoList, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartInfoListWithPrice(Long.parseLong(userId));

        for (CartInfo cartInfoCK : cartInfoList) {
            boolean isMatch = false;
            for (CartInfo cartInfoDB : cartInfoListDB) {

                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCK.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            if (!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //查询数据库,更新缓存
        List<CartInfo> newCartInfoList = loadCartCache(userId);

        //合并选中中状态
        for (CartInfo cartInfo : cartInfoList) {
            if (cartInfo.getIsChecked().equals("1")){
                checkCart(userId,cartInfo.getSkuId(),"1");
            }
        }

        return cartInfoList;
    }


    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        String cartInfoJSON = jedis.hget(userCartKey, skuId);
        CartInfo checkCart = JSON.parseObject(cartInfoJSON, CartInfo.class);
        checkCart.setIsChecked(isChecked);

        String checkCartJson = JSON.toJSONString(checkCart);
        jedis.hset(userCartKey,skuId,checkCartJson);

        String userCheckCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKCART_KEY_SUFFIX;

        if (isChecked.equals("1")){
            jedis.hset(userCheckCartKey,skuId,checkCartJson);
        }else {
            jedis.hdel(userCheckCartKey,skuId);
        }

        jedis.close();
    }


    @Override
    public List<CartInfo> getCheckCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userCheckCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKCART_KEY_SUFFIX;
        List<String> checkCartInfos = jedis.hvals(userCheckCartKey);
        List<CartInfo> checkCartInfoList = new ArrayList<>();


        if (checkCartInfos!=null&&checkCartInfos.size()>0){
            for (String checkCartInfo : checkCartInfos) {
                CartInfo cartInfo = JSON.parseObject(checkCartInfo, CartInfo.class);
                checkCartInfoList.add(cartInfo);
            }
        }
        return checkCartInfoList;
    }
}
