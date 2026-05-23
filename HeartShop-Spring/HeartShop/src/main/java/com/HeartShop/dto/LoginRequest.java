package com.HeartShop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求 DTO
 */
@Data
public class LoginRequest {
    @NotBlank(message = "帳號不能為空")
    private String email; // 支援 email 或使用者名稱

    @NotBlank(message = "密碼不能為空")
    private String password;
}
