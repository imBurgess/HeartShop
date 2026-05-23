package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 調整庫存請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustRequest {
    
    @NotNull(message = "調整數量不能為空")
    private Integer quantityChange; // 調整數量（正數=增加，負數=減少）
    
    private String operator; // 操作人員（可選）
    
    private String remark;   // 備註說明
}
