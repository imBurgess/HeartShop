package com.HeartShop.controller;
import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.CategoryDTO;
import com.HeartShop.dto.CreateCategoryRequest;
import com.HeartShop.dto.UpdateCategoryRequest;
import com.HeartShop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDTO>> getAllCategories(@RequestParam(required = false) Boolean isActive) {
        log.info("API: 查詢所有分類, isActive: {}", isActive);
        List<CategoryDTO> categories = categoryService.getAllCategories(isActive);
        return ApiResponse.success(categories);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.info("API: 查詢分類，ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ApiResponse.success(category);
    }

    @GetMapping("/children")
    public ApiResponse<List<CategoryDTO>> getCategoriesByParentId(
            @RequestParam(required = false) Long parentId) {
        log.info("API: 查詢子分類，父分類 ID: {}", parentId);
        List<CategoryDTO> categories = categoryService.getCategoriesByParentId(parentId);
        return ApiResponse.success(categories);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("API: 建立分類,名稱: {}", request.getNameZh());
        CategoryDTO category = categoryService.createCategory(request);
        return ApiResponse.success("分類建立成功", category);
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("API: 更新分類，ID: {}", id);
        CategoryDTO category = categoryService.updateCategory(id, request);
        return ApiResponse.success("分類更新成功", category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        log.info("API: 刪除分類，ID: {}", id);
        categoryService.deleteCategory(id);
        return ApiResponse.success();
    }
}