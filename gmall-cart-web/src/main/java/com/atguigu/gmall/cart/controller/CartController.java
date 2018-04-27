package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.cookie.CartCookieHandler;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ruochen
 * Date:2018/4/24 0024
 */
@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    ManageSkuService manageSkuService;

    @Autowired
    CartCookieHandler cartCookieHandler;


    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        SkuInfo skuInfo = manageSkuService.getSkuInfo(skuId);

        //判断是否登录
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            //已登录，数据库操作
            cartService.addToCart(userId,skuNum,skuInfo);

        }else {
            //未登录，cookie操作
            cartCookieHandler.addToCart(request,response,skuInfo,skuNum);
        }

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();
        //判断是否登录
        if (userId == null) {
            //未登录，从cookie中获取购物车
            cartInfoList=cartCookieHandler.getCartList(request);

        }else {
            //已登录，判断cookie中是否存在购物车
            cartInfoList=cartCookieHandler.getCartList(request);
            if (cartInfoList != null&&cartInfoList.size()>0) {
                //cookie中存在购物车，将cookie中的购物车合并到数据库
                cartInfoList = cartService.mergeCartList(cartInfoList,userId);
                cartCookieHandler.deleteCartCookie(request,response);
            }
            //从数据库中获取购物车
            cartInfoList = cartService.getCartList(userId);

        }

        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        if (userId != null) {
            cartService.checkCart(userId,skuId,isChecked);
        }else {
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
        if(cartListFromCookie!=null&&cartListFromCookie.size()>0){
            //1 合并到后台
            List<CartInfo> cartList = cartService.mergeCartList(cartListFromCookie, userId);
            //2 cookie中的删除掉
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }
}
