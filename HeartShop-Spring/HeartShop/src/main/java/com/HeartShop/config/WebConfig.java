package com.HeartShop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 將 /uploads/** 的 URL 請求映射到本地 uploads 目錄
        // file:./uploads/ 表示專案根目錄下的 uploads 資料夾
        // 注意：因為設定了 context-path=/api，實際訪問路徑是 /api/uploads/**
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
