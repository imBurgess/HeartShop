package com.HeartShop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品分類 DTO，用於 API 回傳
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Long categoryId;
    private String slug;
    private String nameZh;
    private String nameEn;
    private Long parentId;
    private Integer sortOrder;
    private Boolean isActive;
    private String bannerUrl;  // Banner 圖片 URL
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
