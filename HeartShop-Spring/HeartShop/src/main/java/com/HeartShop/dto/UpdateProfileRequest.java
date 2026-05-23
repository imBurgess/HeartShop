package com.HeartShop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新會員資料請求 DTO
 * 注意：Email 是會員帳號，不允許修改
 */
@Data
public class UpdateProfileRequest {
    @NotBlank(message = "名稱不能為空")
    @Size(min = 2, max = 100, message = "名稱長度需在 2-100 字元之間")
    private String name;

    private String phone;         // 手機號碼（選填）
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;   // 生日（選填）
    
    private String address;       // 地址（選填）
}
