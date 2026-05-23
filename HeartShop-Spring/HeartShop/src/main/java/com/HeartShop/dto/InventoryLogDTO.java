package com.HeartShop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 庫存異動記錄 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLogDTO {
    
    private Long logId;
    private Long productId;
    private String productCode;    // 商品編號（方便前端顯示）
    private String productName;    // 商品名稱（方便前端顯示）
    private String changeType;     // 異動類型
    private String changeTypeDesc; // 異動類型描述
    private Integer quantityBefore;
    private Integer quantityChange;
    private Integer quantityAfter;
    private String operator;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime createdAt;
}
