package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 首頁區塊與商品關聯 Entity，對應 home_block_product 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeBlockProduct {
    
    private Long id;              // id (PK)
    private Long blockId;         // block_id (FK → home_block)
    private Long productId;       // product_id (FK → product)
    private Integer sortOrder;    // 排序（0=主圖, 1,2=子商品）
    private OffsetDateTime createdAt;  // 建立時間
}
