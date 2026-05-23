package com.HeartShop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 建立商品請求 DTO
 */
@Data
public class CreateProductRequest {
    
    @NotNull(message = "分類 ID 不能為空")
    private Long categoryId;
    
    // @NotBlank(message = "商品編號不能為空") -> 改為由後端生成
    @Size(max = 50, message = "商品編號長度不能超過 50")
    private String code;
    
    @NotBlank(message = "商品名稱不能為空")
    @Size(max = 200, message = "商品名稱長度不能超過 200")
    private String name;
    
    @Size(max = 200, message = "英文名稱長度不能超過 200")
    private String nameEn;
    
    @NotNull(message = "價格不能為空")
    @DecimalMin(value = "0.0", message = "價格必須大於等於 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "折扣價必須大於等於 0")
    private BigDecimal discountPrice;
    
    private String description;
    private String sizeInfo;
    private List<String> tags;      // 會轉成字串存入 DB
    
    private Boolean isNew = false;
    private Boolean isActive = true;
    private Integer sortOrder = 0;
    
    private List<String> images; // 圖片 URL 列表

    private Integer initialStock = 0; // 初始庫存數量
}
