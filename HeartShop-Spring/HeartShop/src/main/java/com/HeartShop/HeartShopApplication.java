package com.HeartShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HeartShop 應用程式啟動類別
 * 
 * @SpringBootApplication 包含：
 * - @Configuration: 配置類別
 * - @EnableAutoConfiguration: 自動配置
 * - @ComponentScan: 自動掃描當前包及子包下的元件
 */
@SpringBootApplication
public class HeartShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartShopApplication.class, args);
    }
}
