package com.HeartShop.dto;

import lombok.Data;

/**
 * 商品查詢參數
 */
@Data
public class ProductQueryParams {
    
    private Long categoryId;        // 依分類篩選
    private Boolean isActive;       // 只顯示上架商品
    private Boolean isNew;          // 只顯示新品
    private String keyword;         // 關鍵字搜尋（名稱、編號）
    
    private Integer page = 1;       // 頁碼（從 1 開始）
    private Integer pageSize = 20;  // 每頁筆數
    
    private String sortBy = "sort_order";  // 排序欄位
    private String sortOrder = "ASC";      // 排序方向
}
