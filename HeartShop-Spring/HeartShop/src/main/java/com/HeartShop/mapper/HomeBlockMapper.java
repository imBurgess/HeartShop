package com.HeartShop.mapper;

import com.HeartShop.entity.HomeBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 首頁區塊 Mapper
 */
@Mapper
public interface HomeBlockMapper {
    
    /**
     * 依類型查詢啟用中的區塊（含有效期間判斷、排序）
     */
    List<HomeBlock> selectActiveByType(@Param("type") String type);
    
    /**
     * 依 ID 查詢區塊
     */
    HomeBlock selectById(@Param("blockId") Long blockId);
    
    /**
     * 查詢所有區塊（後台管理用）
     */
    List<HomeBlock> selectAll();
    
    /**
     * 新增區塊
     */
    int insertHomeBlock(HomeBlock homeBlock);
    
    /**
     * 更新區塊
     */
    int updateHomeBlock(HomeBlock homeBlock);
    
    /**
     * 刪除區塊（級聯刪除關聯）
     */
    int deleteById(@Param("blockId") Long blockId);
}
