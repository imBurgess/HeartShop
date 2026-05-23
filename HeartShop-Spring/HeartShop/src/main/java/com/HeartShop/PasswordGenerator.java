package com.HeartShop;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密碼加密工具（僅用於生成測試資料）
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        
        System.out.println("原始密碼: " + password);
        System.out.println("\nBCrypt 加密結果:");
        
        // 生成 3 次（每次結果都不同）
        for (int i = 1; i <= 3; i++) {
            String hashed = encoder.encode(password);
            System.out.println(i + ". " + hashed);
        }
    }
}
