package com.atguigu.gmall.cart.cookie;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.constant.WebConst;
import com.atguigu.gmall.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ruochen
 * Date:2018/4/24 0024
 */
@Component
public class CartCookieHandler {

    @Autowired
    CookieUtil cookieUtil;

    public static final String COOKIE_CART_KEY = "cart";


    public void addToCart(HttpServletRequest request, HttpServletResponse response, SkuInfo skuInfo, String skuNum) {
        String cartJson = cookieUtil.getCookieValue(request, COOKIE_CART_KEY, true);
        List<CartInfo> cartInfoList = new ArrayList<>();


        boolean isMatch = false;
        if (cartJson != null && cartJson.length() > 0) {
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (skuInfo.getId().equals(cartInfo.getSkuId())) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + Integer.parseInt(skuNum));
                    cartInfo.setCartPrice(skuInfo.getPrice());
                    isMatch = true;
                    break;
                }
            }
        }
        if (!isMatch) {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuNum(Integer.parseInt(skuNum));
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setCartPrice(skuInfo.getPrice());

            cartInfoList.add(cartInfo);
        }

        String newCartJson = JSON.toJSONString(cartInfoList);
        cookieUtil.setCookie(request, response, COOKIE_CART_KEY, newCartJson, WebConst.COOKIE_MAXAGE, true);
    }


    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cartJson = cookieUtil.getCookieValue(request, COOKIE_CART_KEY, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);

        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request,HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, COOKIE_CART_KEY);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartList = getCartList(request);
       // List<String> cartJsonList = new ArrayList<>(cartList.size());
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
            //String cartJson = JSON.toJSONString(cartInfo);

        }
        String cartJsons = JSON.toJSONString(cartList);
        cookieUtil.setCookie(request,response,COOKIE_CART_KEY,cartJsons,WebConst.COOKIE_MAXAGE,true);

    }

}
