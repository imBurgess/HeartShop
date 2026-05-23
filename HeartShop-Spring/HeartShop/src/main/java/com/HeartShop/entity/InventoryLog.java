package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 庫存異動記錄 Entity，對應 inventory_log 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {
    
    private Long logId;             // log_id (PK)
    private Long productId;         // product_id (FK)
    private String changeType;      // 異動類型
    private Integer quantityBefore; // 異動前庫存
    private Integer quantityChange; // 異動數量
    private Integer quantityAfter;  // 異動後庫存
    private String operator;        // 操作人員
    private String remark;          // 備註
    private OffsetDateTime createdAt; // 建立時間
}
