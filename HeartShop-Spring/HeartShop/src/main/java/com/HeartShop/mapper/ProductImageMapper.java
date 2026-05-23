package com.HeartShop.mapper;

import com.HeartShop.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductImageMapper {
    
    // 批次新增圖片
    int insertBatch(@Param("images") List<ProductImage> images);
    
    // 根據商品 ID 查詢圖片
    List<ProductImage> selectByProductId(@Param("productId") Long productId);
    
    // 根據商品 ID 刪除所有圖片
    int deleteByProductId(@Param("productId") Long productId);
}
