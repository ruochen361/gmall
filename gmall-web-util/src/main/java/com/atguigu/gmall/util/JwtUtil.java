package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * User: ruochen
 * Date:2018/4/22 0022
 */
public class JwtUtil {

    public static String encode(String key,Map map,String salt){

        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    public static  Map decode(String token,String key,String salt)throws SignatureException{
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        return map;

    }






}
