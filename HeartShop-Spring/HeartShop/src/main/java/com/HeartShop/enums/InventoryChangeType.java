package com.HeartShop.enums;

/**
 * 庫存異動類型列舉
 */
public enum InventoryChangeType {
    
    INITIAL("初始化"),
    ADJUST("手動調整"),
    SALE("銷售"),
    RETURN("退貨");
    
    private final String description;
    
    InventoryChangeType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
