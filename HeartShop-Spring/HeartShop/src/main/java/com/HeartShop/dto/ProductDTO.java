package com.HeartShop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 商品 DTO，用於 API 回傳
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    
    private Long productId;
    private Long categoryId;
    private String categoryName;    // 從 category 表 JOIN 取得
    
    private String code;
    private String name;
    private String nameEn;
    
    private BigDecimal price;
    private BigDecimal discountPrice;
    
    private String description;
    private String sizeInfo;
    private List<String> tags;      // 將字串轉成陣列
    
    private Boolean isNew;
    private Boolean isSoldOut;
    private Boolean isActive;
    
    private Integer viewCount;
    private Integer sortOrder;
    
    // 庫存管理欄位
    private Integer stockQuantity;
    private Integer stockAlertThreshold;
    private Boolean isLowStock; // 是否低庫存（前端判斷用）
    
    private String imageUrl; // 主要圖片 URL (for Frontend)
    private List<String> images; // 圖片 URL 列表
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime updatedAt;
}
