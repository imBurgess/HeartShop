package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.CreateHomeBlockRequest;
import com.HeartShop.dto.HomeBlockDTO;
import com.HeartShop.dto.UpdateHomeBlockRequest;
import com.HeartShop.service.HomeBlockService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首頁區塊 Controller
 */
@Slf4j
@RestController
@RequestMapping("/home-blocks")  // 移除 /api，因為 context-path 已包含
public class HomeBlockController {
    
    private final HomeBlockService homeBlockService;
    
    public HomeBlockController(HomeBlockService homeBlockService) {
        this.homeBlockService = homeBlockService;
    }
    
    /**
     * 依類型查詢啟用中的區塊（前端首頁使用）
     * GET /api/home-blocks/type/{type}
     * @param type 類型: CAROUSEL, MEMBER_BANNER, PRODUCT_RECOMMEND, GENERAL_ANNOUNCEMENT
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<HomeBlockDTO>>> getBlocksByType(@PathVariable String type) {
        log.debug("查詢啟用中的區塊,類型: {}", type);
        
        List<HomeBlockDTO> blocks = homeBlockService.getActiveBlocksByType(type);
        return ResponseEntity.ok(ApiResponse.success(blocks));
    }
    
    /**
     * 依 ID 查詢區塊詳情
     * GET /api/home-blocks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HomeBlockDTO>> getBlockById(@PathVariable Long id) {
        log.debug("查詢區塊,ID: {}", id);
        
        HomeBlockDTO block = homeBlockService.getBlockById(id);
        return ResponseEntity.ok(ApiResponse.success(block));
    }
    
    /**
     * 查詢所有區塊（後台管理用）
     * GET /api/home-blocks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeBlockDTO>>> getAllBlocks() {
        log.debug("查詢所有區塊");
        
        List<HomeBlockDTO> blocks = homeBlockService.getAllBlocks();
        return ResponseEntity.ok(ApiResponse.success(blocks));
    }
    
    /**
     * 新增區塊
     * POST /api/home-blocks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HomeBlockDTO>> createBlock(
            @Valid @RequestBody CreateHomeBlockRequest request) {
        log.info("新增區塊,類型: {}", request.getType());
        
        HomeBlockDTO block = homeBlockService.createBlock(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("區塊新增成功", block));
    }
    
    /**
     * 更新區塊
     * PUT /api/home-blocks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HomeBlockDTO>> updateBlock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHomeBlockRequest request) {
        log.info("更新區塊,ID: {}", id);
        
        HomeBlockDTO block = homeBlockService.updateBlock(id, request);
        return ResponseEntity.ok(ApiResponse.success("區塊更新成功", block));
    }
    
    /**
     * 刪除區塊
     * DELETE /api/home-blocks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(@PathVariable Long id) {
        log.info("刪除區塊,ID: {}", id);
        
        homeBlockService.deleteBlock(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("區塊刪除成功", null));
    }
}
