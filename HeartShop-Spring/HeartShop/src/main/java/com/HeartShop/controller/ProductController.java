package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.CreateProductRequest;
import com.HeartShop.dto.ProductDTO;
import com.HeartShop.dto.ProductQueryParams;
import com.HeartShop.dto.UpdateProductRequest;
import com.HeartShop.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品 Controller
 */
@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 查詢商品列表
     * GET /api/products?page=1&pageSize=20&categoryId=1&isActive=true&keyword=測試
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProducts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) Boolean isNew,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize,
        @RequestParam(required = false, defaultValue = "sort_order") String sortBy,
        @RequestParam(required = false, defaultValue = "ASC") String sortOrder
    ) {
        log.debug("查詢商品列表,page: {}, pageSize: {}, categoryId: {}", page, pageSize, categoryId);
        
        ProductQueryParams params = new ProductQueryParams();
        params.setCategoryId(categoryId);
        params.setIsActive(isActive);
        params.setIsNew(isNew);
        params.setKeyword(keyword);
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy(sortBy);
        params.setSortOrder(sortOrder);
        
        Map<String, Object> result = productService.getProducts(params);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 根據 ID 查詢商品
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        log.debug("查詢商品,ID: {}", id);
        
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    /**
     * 根據商品編號查詢
     * GET /api/products/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductByCode(@PathVariable String code) {
        log.debug("查詢商品,編號: {}", code);
        
        ProductDTO product = productService.getProductByCode(code);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    /**
     * 建立商品
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
        @Valid @RequestBody CreateProductRequest request
    ) {
        log.info("建立商品: {}", request.getName());
        
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("商品建立成功", product));
    }
    
    /**
     * 更新商品
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("更新商品,ID: {}", id);
        
        ProductDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("商品更新成功", product));
    }
    
    /**
     * 刪除商品
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("刪除商品,ID: {}", id);
        
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("商品刪除成功", null));
    }
}
