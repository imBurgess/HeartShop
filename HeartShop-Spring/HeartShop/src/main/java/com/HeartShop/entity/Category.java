package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 商品分類 Entity，對應 category 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    private Long categoryId;      // 對應 category_id
    private String slug;          // URL slug
    private String nameZh;        // 中文名稱
    private String nameEn;        // 英文名稱
    private Long parentId;        // 父分類 ID
    private Integer sortOrder;    // 排序順序
    private Boolean isActive;     // 是否啟用
    private String bannerUrl;     // Banner 圖片 URL
    private OffsetDateTime createdAt;  // 建立時間
}