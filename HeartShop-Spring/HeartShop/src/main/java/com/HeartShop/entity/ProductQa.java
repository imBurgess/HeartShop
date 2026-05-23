package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQa {
    private Long qaId;
    private Long productId;
    private String productName;
    private Long memberId;
    private String memberName;
    private String question;
    private String answer;
    private Boolean isPublic;
    private OffsetDateTime createdAt;
    private OffsetDateTime answeredAt;
}
