package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 會員實體類
 * 對應資料表：member
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long memberId;
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthday;
    private String address;
    private String role;           // ADMIN, VIP, CUSTOMER
    private String status;         // ACTIVE, INACTIVE
    private Boolean subscribeEdm;
    private Integer bonusPoints;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
