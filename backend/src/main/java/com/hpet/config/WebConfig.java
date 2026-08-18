package com.hpet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * ImageStorageService가 로컬 디스크에 저장한 이미지를 /files/** 경로로 그대로 서빙해준다.
 * (예: 저장 경로 uploads/dose/abcd.jpg -> 브라우저에서 http://localhost:8080/files/dose/abcd.jpg 로 접근)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.local.root:./uploads}")
    private String storageRootPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(storageRootPath).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
