package com.HeartShop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新商品請求 DTO
 */
@Data
public class UpdateProductRequest {
    
    private Long categoryId;
    
    @Size(max = 50, message = "商品編號長度不能超過 50")
    private String code;
    
    @Size(max = 200, message = "商品名稱長度不能超過 200")
    private String name;
    
    @Size(max = 200, message = "英文名稱長度不能超過 200")
    private String nameEn;
    
    @DecimalMin(value = "0.0", message = "價格必須大於等於 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "折扣價必須大於等於 0")
    private BigDecimal discountPrice;
    
    private String description;
    private String sizeInfo;
    private List<String> tags;
    
    private Boolean isNew;
    private Boolean isSoldOut;
    private Boolean isActive;
    private Integer sortOrder;
    
    private List<String> images; // 圖片 URL 列表

    private Integer stock; // 調整後庫存數量（null 表示不調整）
}
