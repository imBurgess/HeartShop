package com.HeartShop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立商品分類的請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    
    @NotBlank(message = "slug 不可為空")
    @Size(max = 50, message = "slug 長度不可超過 50 字元")
    private String slug;
    
    @NotBlank(message = "中文名稱不可為空")
    @Size(max = 100, message = "中文名稱長度不可超過 100 字元")
    private String nameZh;
    
    @Size(max = 100, message = "英文名稱長度不可超過 100 字元")
    private String nameEn;
    
    private Long parentId;
    private Integer sortOrder;
    private Boolean isActive;

    @Size(max = 500, message = "Banner URL 長度不可超過 500 字元")
    private String bannerUrl;
}