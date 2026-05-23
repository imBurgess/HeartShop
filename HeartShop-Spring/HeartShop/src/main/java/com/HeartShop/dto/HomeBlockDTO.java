package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首頁區塊 DTO，用於回傳給前端
 * 包含關聯的商品資料（用於 PRODUCT_RECOMMEND 類型）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeBlockDTO {
    
    private Long blockId;
    private String type;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 關聯的商品資料（僅 PRODUCT_RECOMMEND 類型會有）
    private List<ProductDTO> products;
}
