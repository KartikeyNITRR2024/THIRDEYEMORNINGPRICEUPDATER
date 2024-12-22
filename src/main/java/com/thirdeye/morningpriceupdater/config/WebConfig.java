package com.thirdeye.morningpriceupdater.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.thirdeye.morningpriceupdater.interceptors.AuthorizationInterceptor;
import com.thirdeye.morningpriceupdater.utils.AllMicroservicesData;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    
    @Autowired
	AllMicroservicesData allMicroservicesData;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/stocklist/**");
    }
}

