package com.HeartShop.service;

import com.HeartShop.dto.CategoryDTO;
import com.HeartShop.dto.CreateCategoryRequest;
import com.HeartShop.dto.UpdateCategoryRequest;
import com.HeartShop.entity.Category;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.exception.ResourceNotFoundException;
import com.HeartShop.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分類 Service
 */
@Slf4j
@Service
public class CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    
    /**
     * 查詢所有分類
     */
    /**
     * 查詢所有分類
     */
    public List<CategoryDTO> getAllCategories(Boolean isActive) {
        log.debug("查詢所有分類, isActive: {}", isActive);
        List<Category> categories = categoryMapper.selectAllCategories(isActive);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根據 ID 查詢分類
     */
    public CategoryDTO getCategoryById(Long id) {
        log.debug("查詢分類,ID: {}", id);
        Category category = categoryMapper.selectCategoryById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分類", id);
        }
        return convertToDTO(category);
    }
    
    /**
     * 根據父分類 ID 查詢子分類
     */
    public List<CategoryDTO> getCategoriesByParentId(Long parentId) {
        log.debug("查詢子分類,父分類 ID: {}", parentId);
        List<Category> categories = categoryMapper.selectCategoriesByParentId(parentId);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 建立分類
     */
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.info("建立分類: {}", request.getNameZh());
        
        // 檢查 slug 是否重複
        if (categoryMapper.countByName(request.getSlug(), null) > 0) {
            throw new BusinessException("分類 slug 已存在：" + request.getSlug());
        }
        
        // 檢查父分類是否存在
        if (request.getParentId() != null) {
            Category parentCategory = categoryMapper.selectCategoryById(request.getParentId());
            if (parentCategory == null) {
                throw new ResourceNotFoundException("父分類", request.getParentId());
            }
        }
        
        // 建立 Category Entity
        Category category = new Category();
        category.setSlug(request.getSlug());
        category.setNameZh(request.getNameZh());
        category.setNameEn(request.getNameEn());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        categoryMapper.insertCategory(category);
        
        log.info("分類建立成功,ID: {}", category.getCategoryId());
        return convertToDTO(category);
    }
    
    /**
     * 更新分類
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("更新分類,ID: {}", id);
        
        // 檢查分類是否存在
        Category existingCategory = categoryMapper.selectCategoryById(id);
        if (existingCategory == null) {
            throw new ResourceNotFoundException("分類", id);
        }
        
        // 檢查 slug 是否重複
        if (request.getSlug() != null && categoryMapper.countByName(request.getSlug(), id) > 0) {
            throw new BusinessException("分類 slug 已存在：" + request.getSlug());
        }
        
        // 檢查父分類
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("父分類不可為自己");
            }
            Category parentCategory = categoryMapper.selectCategoryById(request.getParentId());
            if (parentCategory == null) {
                throw new ResourceNotFoundException("父分類", request.getParentId());
            }
        }
        
        // 更新欄位
        if (request.getSlug() != null) {
            existingCategory.setSlug(request.getSlug());
        }
        if (request.getNameZh() != null) {
            existingCategory.setNameZh(request.getNameZh());
        }
        if (request.getNameEn() != null) {
            existingCategory.setNameEn(request.getNameEn());
        }
        if (request.getParentId() != null) {
            existingCategory.setParentId(request.getParentId());
        }
        if (request.getSortOrder() != null) {
            existingCategory.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            existingCategory.setIsActive(request.getIsActive());
        }
        // 更新 Banner URL（允許空字串以清除 banner）
        if (request.getBannerUrl() != null) {
            existingCategory.setBannerUrl(request.getBannerUrl());
        }
        
        categoryMapper.updateCategory(existingCategory);
        
        log.info("分類更新成功,ID: {}", id);
        return convertToDTO(categoryMapper.selectCategoryById(id));
    }
    
    /**
     * 刪除分類
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("刪除分類,ID: {}", id);
        
        // 檢查分類是否存在
        Category category = categoryMapper.selectCategoryById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分類", id);
        }
        
        // 檢查是否有子分類
        List<Category> childCategories = categoryMapper.selectCategoriesByParentId(id);
        if (!childCategories.isEmpty()) {
            throw new BusinessException("此分類下有子分類,無法刪除");
        }
        
        categoryMapper.deleteCategory(id);
        log.info("分類刪除成功,ID: {}", id);
    }
    
    /**
     * 將 Entity 轉換為 DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setSlug(category.getSlug());
        dto.setNameZh(category.getNameZh());
        dto.setNameEn(category.getNameEn());
        dto.setParentId(category.getParentId());
        dto.setSortOrder(category.getSortOrder());
        dto.setIsActive(category.getIsActive());
        dto.setBannerUrl(category.getBannerUrl());  // 複製 Banner URL
        if (category.getCreatedAt() != null) {
            dto.setCreatedAt(category.getCreatedAt().toLocalDateTime());
        }
        return dto;
    }
}