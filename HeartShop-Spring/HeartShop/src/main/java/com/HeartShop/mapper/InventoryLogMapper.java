package com.HeartShop.mapper;

import com.HeartShop.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryLogMapper {
    
    /**
     * 新增庫存異動記錄
     */
    void insertLog(InventoryLog log);
    
    /**
     * 根據商品 ID 查詢異動記錄
     */
    List<InventoryLog> selectByProductId(
        @Param("productId") Long productId,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );
    
    /**
     * 計算商品異動記錄總數
     */
    int countByProductId(@Param("productId") Long productId);
    
    /**
     * 查詢所有異動記錄（支援篩選與分頁）
     */
    List<InventoryLog> selectLogs(
        @Param("productId") Long productId,
        @Param("changeType") String changeType,
        @Param("keyword") String keyword,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );
    
    /**
     * 計算異動記錄總數
     */
    int countLogs(
        @Param("productId") Long productId,
        @Param("changeType") String changeType,
        @Param("keyword") String keyword
    );
}
