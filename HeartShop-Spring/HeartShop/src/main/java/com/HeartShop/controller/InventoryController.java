package com.HeartShop.controller;

import com.HeartShop.dto.InventoryAdjustRequest;
import com.HeartShop.dto.InventoryQueryParams;
import com.HeartShop.service.InventoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 庫存管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * 查詢庫存列表
     * GET /api/inventory?categoryId=1&keyword=手錶&lowStockOnly=true&page=1&pageSize=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryList(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean lowStockOnly,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        log.info("查詢庫存列表 - categoryId: {}, keyword: {}, lowStockOnly: {}", categoryId, keyword, lowStockOnly);
        
        InventoryQueryParams params = new InventoryQueryParams();
        params.setCategoryId(categoryId);
        params.setKeyword(keyword);
        params.setLowStockOnly(lowStockOnly);
        params.setSortBy(sortBy);
        params.setSortOrder(sortOrder);
        params.setPage(page);
        params.setPageSize(pageSize);
        
        Map<String, Object> result = inventoryService.getInventoryList(params);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 取得低庫存警示列表
     * GET /api/inventory/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getLowStockAlerts() {
        log.info("查詢低庫存警示");
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", inventoryService.getLowStockAlerts());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 調整商品庫存
     * POST /api/inventory/{productId}/adjust
     * Body: { "quantityChange": 50, "operator": "admin", "remark": "補貨" }
     */
    @PostMapping("/{productId}/adjust")
    public ResponseEntity<Map<String, Object>> adjustStock(
        @PathVariable Long productId,
        @Valid @RequestBody InventoryAdjustRequest request
    ) {
        log.info("調整庫存 - 商品ID: {}, 調整數量: {}", productId, request.getQuantityChange());
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", inventoryService.adjustStock(productId, request));
        response.put("message", "庫存調整成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查詢商品庫存異動記錄
     * GET /api/inventory/{productId}/logs?page=1&pageSize=20
     */
    @GetMapping("/{productId}/logs")
    public ResponseEntity<Map<String, Object>> getInventoryLogs(
        @PathVariable Long productId,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        log.info("查詢庫存異動記錄 - 商品ID: {}", productId);
        
        Map<String, Object> result = inventoryService.getInventoryLogs(productId, page, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        
        return ResponseEntity.ok(response);
    }
}
