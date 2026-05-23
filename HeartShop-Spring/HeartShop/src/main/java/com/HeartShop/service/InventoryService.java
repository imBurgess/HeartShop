package com.HeartShop.service;

import com.HeartShop.dto.InventoryAdjustRequest;
import com.HeartShop.dto.InventoryLogDTO;
import com.HeartShop.dto.InventoryQueryParams;
import com.HeartShop.dto.ProductDTO;
import com.HeartShop.entity.InventoryLog;
import com.HeartShop.entity.Product;
import com.HeartShop.enums.InventoryChangeType;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.exception.ResourceNotFoundException;
import com.HeartShop.mapper.InventoryLogMapper;
import com.HeartShop.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 庫存管理 Service
 */
@Slf4j
@Service
public class InventoryService {
    
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final ProductService productService;
    
    public InventoryService(ProductMapper productMapper, 
                          InventoryLogMapper inventoryLogMapper,
                          ProductService productService) {
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.productService = productService;
    }
    
    /**
     * 查詢庫存列表
     */
    public Map<String, Object> getInventoryList(InventoryQueryParams params) {
        log.debug("查詢庫存列表,參數: {}", params);
        
        // 驗證分頁參數
        int page = (params.getPage() != null && params.getPage() > 0) ? params.getPage() : 1;
        int pageSize = (params.getPageSize() != null && params.getPageSize() > 0) ? params.getPageSize() : 20;
        if (pageSize > 100) {
            pageSize = 100;
        }
        int offset = (page - 1) * pageSize;
        
        // 查詢商品
        List<Product> products = productMapper.selectProducts(
            params.getCategoryId(),
            true, // 只查詢上架商品
            null,
            params.getKeyword(),
            params.getSortBy(),
            params.getSortOrder(),
            pageSize,
            offset
        );
        
        // 計算總數
        int total = productMapper.countProducts(
            params.getCategoryId(),
            true,
            null,
            params.getKeyword()
        );
        
        // 轉換為 DTO 並添加低庫存標記
        List<ProductDTO> productDTOs = products.stream()
            .filter(p -> {
                // 如果只查詢低庫存，則過濾
                if (Boolean.TRUE.equals(params.getLowStockOnly())) {
                    Integer stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                    Integer threshold = p.getStockAlertThreshold() != null ? p.getStockAlertThreshold() : 10;
                    return stock <= threshold;
                }
                return true;
            })
            .map(productService::convertToDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        
        return result;
    }
    
    /**
     * 調整商品庫存
     */
    @Transactional
    public ProductDTO adjustStock(Long productId, InventoryAdjustRequest request) {
        log.info("調整庫存,商品ID: {}, 調整數量: {}", productId, request.getQuantityChange());
        
        // 1. 檢查商品是否存在
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ResourceNotFoundException("商品", productId);
        }
        
        // 2. 計算新庫存
        Integer currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        Integer newStock = currentStock + request.getQuantityChange();
        
        if (newStock < 0) {
            throw new BusinessException("庫存不足，無法調整");
        }
        
        // 3. 更新庫存
        productMapper.updateStock(productId, newStock);
        
        // 4. 記錄異動
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setProductId(productId);
        inventoryLog.setChangeType(InventoryChangeType.ADJUST.name());
        inventoryLog.setQuantityBefore(currentStock);
        inventoryLog.setQuantityChange(request.getQuantityChange());
        inventoryLog.setQuantityAfter(newStock);
        inventoryLog.setOperator(request.getOperator());
        inventoryLog.setRemark(request.getRemark());
        inventoryLogMapper.insertLog(inventoryLog);
        
        log.info("庫存調整成功,商品ID: {}, 原庫存: {}, 新庫存: {}", productId, currentStock, newStock);
        
        // 5. 返回更新後的商品資料
        return productService.getProductById(productId);
    }
    
    /**
     * 查詢低庫存警示列表
     */
    public List<ProductDTO> getLowStockAlerts() {
        log.debug("查詢低庫存警示");
        
        List<Product> lowStockProducts = productMapper.selectLowStockProducts();
        
        return lowStockProducts.stream()
            .map(productService::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 查詢商品庫存異動記錄
     */
    public Map<String, Object> getInventoryLogs(Long productId, Integer page, Integer pageSize) {
        log.debug("查詢庫存異動記錄,商品ID: {}", productId);
        
        // 驗證分頁參數
        page = (page != null && page > 0) ? page : 1;
        pageSize = (pageSize != null && pageSize > 0) ? pageSize : 20;
        if (pageSize > 100) {
            pageSize = 100;
        }
        int offset = (page - 1) * pageSize;
        
        // 查詢記錄
        List<InventoryLog> logs = inventoryLogMapper.selectByProductId(productId, pageSize, offset);
        int total = inventoryLogMapper.countByProductId(productId);
        
        // 轉換為 DTO
        List<InventoryLogDTO> logDTOs = logs.stream()
            .map(this::convertLogToDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", logDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        
        return result;
    }
    
    /**
     * 將 InventoryLog 轉換為 DTO
     */
    private InventoryLogDTO convertLogToDTO(InventoryLog log) {
        InventoryLogDTO dto = new InventoryLogDTO();
        dto.setLogId(log.getLogId());
        dto.setProductId(log.getProductId());
        dto.setChangeType(log.getChangeType());
        
        // 取得異動類型描述
        try {
            InventoryChangeType changeType = InventoryChangeType.valueOf(log.getChangeType());
            dto.setChangeTypeDesc(changeType.getDescription());
        } catch (IllegalArgumentException e) {
            dto.setChangeTypeDesc(log.getChangeType());
        }
        
        dto.setQuantityBefore(log.getQuantityBefore());
        dto.setQuantityChange(log.getQuantityChange());
        dto.setQuantityAfter(log.getQuantityAfter());
        dto.setOperator(log.getOperator());
        dto.setRemark(log.getRemark());
        dto.setCreatedAt(log.getCreatedAt());
        
        // 查詢商品資訊
        Product product = productMapper.selectProductById(log.getProductId());
        if (product != null) {
            dto.setProductCode(product.getCode());
            dto.setProductName(product.getName());
        }
        
        return dto;
    }
}
