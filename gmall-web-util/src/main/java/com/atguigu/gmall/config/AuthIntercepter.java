package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.constant.WebConst;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * User: ruochen
 * Date:2018/4/22 0022
 */
@Component
public class AuthIntercepter extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");
        if (token != null) {
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        } else {
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        if (token != null) {
            //解析token，并在页面显示昵称

            Map map = getUserInfoByToekn(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);

        }

        //检查是否需要验证用户已经登录
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (loginRequireAnnotation != null) {

            String remoteAddr = request.getHeader("x-forwarded-for");

            String result=null;
            if (token!=null){
                result = HttpclientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIP=" + remoteAddr);
            }

            if (result!=null&&"success".equals(result)) {
                Map map = getUserInfoByToekn(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            } else {
                if (loginRequireAnnotation.autoRedirect()) {
                    String requestURL = request.getRequestURL().toString();
                    String originUrl = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + originUrl);
                    return false;
                }
            }
        }
        return true;
    }


    private Map getUserInfoByToekn(String token){
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }
}
