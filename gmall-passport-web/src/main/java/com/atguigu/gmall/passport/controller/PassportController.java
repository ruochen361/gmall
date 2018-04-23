package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UsermanageService;
import com.atguigu.gmall.util.JwtUtil;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ruochen
 * Date:2018/4/21 0021
 */
@Controller
public class PassportController {

    @Reference
    UsermanageService usermanageService;

    @Value("token.key")
    String TOKEN_KEY;

    @RequestMapping("index.html")
    public String index(HttpServletRequest httpServletRequest) {
        String originUrl = httpServletRequest.getParameter("originUrl");
        httpServletRequest.setAttribute("originUrl", originUrl);

        return "index";
    }


    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest httpServletRequest) {
        String remoteAddr
                = httpServletRequest.getHeader("x-forwarded-for");

        UserInfo loginUserInfo = usermanageService.login(userInfo);

        if (loginUserInfo != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", loginUserInfo.getId());
            map.put("nickName", loginUserInfo.getNickName());
            String token = JwtUtil.encode(TOKEN_KEY, map, remoteAddr);
            return token;

        } else {
            return "fail";
        }

    }


    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getParameter("token");
        String currentIP = httpServletRequest.getParameter("currentIP");

        //检查token
        Map<String, Object> map = null;
        try {
            map = JwtUtil.decode(token, TOKEN_KEY, currentIP);
        } catch (SignatureException e) {
            return "fail";
        }

        //检查redis
        if (map != null) {
            String userId = (String) map.get("userId");
            UserInfo userInfo = usermanageService.verify(userId);
            if (userInfo != null) {
                return "success";
            }
        }
        return "fail";
    }

}
