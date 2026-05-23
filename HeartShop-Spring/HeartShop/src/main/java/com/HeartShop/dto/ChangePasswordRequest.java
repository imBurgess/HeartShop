package com.HeartShop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密碼請求 DTO
 */
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "舊密碼不能為空")
    private String oldPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 20, message = "密碼長度需在 6-20 字元之間")
    private String newPassword;
}
