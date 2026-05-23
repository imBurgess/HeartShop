package com.HeartShop.mapper;

import com.HeartShop.entity.HomeBlockProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 首頁區塊與商品關聯 Mapper
 */
@Mapper
public interface HomeBlockProductMapper {
    
    /**
     * 查詢區塊的關聯商品 ID（依 sort_order 排序）
     */
    List<HomeBlockProduct> selectByBlockId(@Param("blockId") Long blockId);
    
    /**
     * 批次新增關聯
     */
    int insertBatch(@Param("list") List<HomeBlockProduct> list);
    
    /**
     * 刪除區塊的所有關聯
     */
    int deleteByBlockId(@Param("blockId") Long blockId);
}
