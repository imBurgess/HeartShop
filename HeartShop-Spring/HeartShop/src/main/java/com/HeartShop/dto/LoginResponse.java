package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登入回應 DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private MemberDTO member;
}
