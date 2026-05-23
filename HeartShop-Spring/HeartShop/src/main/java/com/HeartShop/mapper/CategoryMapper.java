package com.HeartShop.mapper;

import com.HeartShop.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> selectAllCategories(@Param("isActive") Boolean isActive);

    Category selectCategoryById(@Param("id") Long id);

    List<Category> selectCategoriesByParentId(@Param("parentId") Long parentId);

    int insertCategory(Category category);

    int updateCategory(Category category);

    int deleteCategory(@Param("id") Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

}
