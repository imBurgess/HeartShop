package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 首頁區塊 Entity，對應 home_block 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeBlock {
    
    private Long blockId;           // block_id (PK)
    private String type;            // 類型: CAROUSEL, MEMBER_BANNER, PRODUCT_RECOMMEND, GENERAL_ANNOUNCEMENT
    private String title;           // 標題
    private String subtitle;        // 副標題
    private String imageUrl;        // 圖片 URL
    private String linkUrl;         // 點擊連結
    private Integer sortOrder;      // 排序
    private Boolean isActive;       // 是否啟用
    private OffsetDateTime startTime;  // 開始時間
    private OffsetDateTime endTime;    // 結束時間
    private OffsetDateTime createdAt;  // 建立時間
    private OffsetDateTime updatedAt;  // 更新時間
}
