package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 商品 Entity，對應 product 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    private Long productId;         // product_id (PK)
    private Long categoryId;        // category_id (FK)
    private String code;            // 商品編號
    private String name;            // 商品名稱（中文）
    private String nameEn;          // 商品名稱（英文）
    
    private BigDecimal price;       // 售價
    private BigDecimal discountPrice; // 折扣價
    
    private String description;     // 商品描述
    private String sizeInfo;        // 尺寸資訊
    private String tags;            // 標籤（逗號分隔或 JSON 字串）
    
    private Boolean isNew;          // 是否新品
    private Boolean isSoldOut;      // 是否售完
    private Boolean isActive;       // 是否上架
    
    private Integer viewCount;      // 瀏覽次數
    private Integer sortOrder;      // 排序順序
    
    // 庫存管理欄位
    private Integer stockQuantity;  // 當前庫存數量
    private Integer stockAlertThreshold; // 庫存警戒線
    private OffsetDateTime lastStockUpdateAt; // 最後庫存更新時間
    
    private OffsetDateTime createdAt;  // 建立時間
    private OffsetDateTime updatedAt;  // 更新時間
}
