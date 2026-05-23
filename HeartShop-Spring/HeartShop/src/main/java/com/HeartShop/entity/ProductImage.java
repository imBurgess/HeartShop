package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 商品圖片 Entity，對應 product_image 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    
    private Long imageId;       // image_id (PK)
    private Long productId;     // product_id (FK)
    private String imageUrl;    // image_url
    private Integer sortOrder;  // sort_order
    private OffsetDateTime createdAt; // created_at
}
