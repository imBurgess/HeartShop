package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新增首頁區塊請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHomeBlockRequest {
    
    private String type;            // 必填: CAROUSEL, MEMBER_BANNER, PRODUCT_RECOMMEND, GENERAL_ANNOUNCEMENT
    private String title;
    private String subtitle;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 商品 ID 列表（僅 PRODUCT_RECOMMEND 類型需要）
    private List<Long> productIds;
}
