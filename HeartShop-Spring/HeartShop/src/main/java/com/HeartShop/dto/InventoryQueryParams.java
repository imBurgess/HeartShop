package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 庫存查詢參數
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryQueryParams {
    
    private Long categoryId;      // 分類篩選
    private String keyword;       // 關鍵字（商品編號或名稱）
    private Boolean lowStockOnly; // 只顯示低庫存商品
    private String sortBy;        // 排序欄位: stock, updateTime
    private String sortOrder;     // 排序方向: asc, desc
    private Integer page;         // 分頁頁數
    private Integer pageSize;     // 每頁筆數
}
