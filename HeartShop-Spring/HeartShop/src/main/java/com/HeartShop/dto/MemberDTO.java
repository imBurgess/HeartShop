package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員資料 DTO（回傳給前端，不包含密碼）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long memberId;
    private String account;    // 對應 email（前端 account 欄位）
    private String email;
    private String name;
    private String role;       // ADMIN | VIP | CUSTOMER
    private String createdAt;  // ISO 8601 字串格式
}
