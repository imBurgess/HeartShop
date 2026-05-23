package com.HeartShop.mapper;

import com.HeartShop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    
    /**
     * 查詢商品列表（支援分頁與篩選）
     */
    List<Product> selectProducts(
        @Param("categoryId") Long categoryId,
        @Param("isActive") Boolean isActive,
        @Param("isNew") Boolean isNew,
        @Param("keyword") String keyword,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );
    
    /**
     * 計算符合條件的商品總數
     */
    int countProducts(
        @Param("categoryId") Long categoryId,
        @Param("isActive") Boolean isActive,
        @Param("isNew") Boolean isNew,
        @Param("keyword") String keyword
    );
    
    /**
     * 根據 ID 查詢商品（含分類名稱）
     */
    Product selectProductById(@Param("id") Long id);
    
    /**
     * 根據商品編號查詢
     */
    Product selectProductByCode(@Param("code") String code);
    
    /**
     * 新增商品
     */
    void insertProduct(Product product);
    
    /**
     * 更新商品
     */
    void updateProduct(Product product);
    
    /**
     * 刪除商品
     */
    void deleteProduct(@Param("id") Long id);
    
    /**
     * 檢查商品編號是否重複
     */
    int countByCode(
        @Param("code") String code,
        @Param("excludeId") Long excludeId
    );
    
    /**
     * 增加瀏覽次數
     */
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 更新庫存數量
     */
    void updateStock(
        @Param("id") Long id,
        @Param("quantity") Integer quantity
    );
    
    /**
     * 查詢低庫存商品
     */
    List<Product> selectLowStockProducts();
}
