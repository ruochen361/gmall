package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

/**
 * User: ruochen
 * Date:2018/4/22 0022
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter{

    @Autowired
    AuthIntercepter authIntercepter;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authIntercepter).addPathPatterns("/**");
        super.addInterceptors(registry);

    }

}
