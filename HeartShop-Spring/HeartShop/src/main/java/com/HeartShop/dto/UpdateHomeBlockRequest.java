package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新首頁區塊請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHomeBlockRequest {
    
    private String type;
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
