package com.HeartShop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 註冊請求 DTO
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "名稱不能為空")
    @Size(min = 2, max = 100, message = "名稱長度需在 2-100 字元之間")
    private String name;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 20, message = "密碼長度需在 6-20 字元之間")
    private String password;
}
