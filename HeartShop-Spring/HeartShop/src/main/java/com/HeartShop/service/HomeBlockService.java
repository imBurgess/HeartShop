package com.HeartShop.service;

import com.HeartShop.dto.CreateHomeBlockRequest;
import com.HeartShop.dto.HomeBlockDTO;
import com.HeartShop.dto.ProductDTO;
import com.HeartShop.dto.UpdateHomeBlockRequest;
import com.HeartShop.entity.HomeBlock;
import com.HeartShop.entity.HomeBlockProduct;
import com.HeartShop.entity.Product;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.exception.ResourceNotFoundException;
import com.HeartShop.mapper.HomeBlockMapper;
import com.HeartShop.mapper.HomeBlockProductMapper;
import com.HeartShop.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首頁區塊 Service
 */
@Slf4j
@Service
public class HomeBlockService {
    
    private final HomeBlockMapper homeBlockMapper;
    private final HomeBlockProductMapper homeBlockProductMapper;
    private final ProductMapper productMapper;
    
    public HomeBlockService(
            HomeBlockMapper homeBlockMapper,
            HomeBlockProductMapper homeBlockProductMapper,
            ProductMapper productMapper) {
        this.homeBlockMapper = homeBlockMapper;
        this.homeBlockProductMapper = homeBlockProductMapper;
        this.productMapper = productMapper;
    }
    
    /**
     * 依類型查詢啟用中的區塊（前端首頁使用）
     */
    public List<HomeBlockDTO> getActiveBlocksByType(String type) {
        log.debug("查詢啟用中的區塊,類型: {}", type);
        
        List<HomeBlock> blocks = homeBlockMapper.selectActiveByType(type);
        
        return blocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 依 ID 查詢區塊詳情
     */
    public HomeBlockDTO getBlockById(Long blockId) {
        log.debug("查詢區塊,ID: {}", blockId);
        
        HomeBlock block = homeBlockMapper.selectById(blockId);
        if (block == null) {
            throw new ResourceNotFoundException("區塊", blockId);
        }
        
        return convertToDTO(block);
    }
    
    /**
     * 查詢所有區塊（後台管理用）
     */
    public List<HomeBlockDTO> getAllBlocks() {
        log.debug("查詢所有區塊");
        
        List<HomeBlock> blocks = homeBlockMapper.selectAll();
        
        return blocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 新增區塊
     */
    @Transactional
    public HomeBlockDTO createBlock(CreateHomeBlockRequest request) {
        log.info("新增區塊,類型: {}", request.getType());
        
        // 1. 驗證必填欄位
        validateBlockRequest(request.getType(), request.getProductIds());
        
        // 2. 建立 HomeBlock Entity
        HomeBlock block = new HomeBlock();
        block.setType(request.getType());
        block.setTitle(request.getTitle());
        block.setSubtitle(request.getSubtitle());
        block.setImageUrl(request.getImageUrl());
        block.setLinkUrl(request.getLinkUrl());
        block.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        block.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        // 轉換 LocalDateTime 為 OffsetDateTime
        if (request.getStartTime() != null) {
            block.setStartTime(request.getStartTime().atOffset(java.time.ZoneOffset.UTC));
        }
        if (request.getEndTime() != null) {
            block.setEndTime(request.getEndTime().atOffset(java.time.ZoneOffset.UTC));
        }
        
        // 3. 儲存 home_block
        homeBlockMapper.insertHomeBlock(block);
        
        // 4. 如果是 PRODUCT_RECOMMEND 類型，建立商品關聯
        if ("PRODUCT_RECOMMEND".equals(request.getType()) && request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            saveBlockProducts(block.getBlockId(), request.getProductIds());
        }
        
        log.info("區塊新增成功,ID: {}, 類型: {}", block.getBlockId(), block.getType());
        
        return convertToDTO(block);
    }
    
    /**
     * 更新區塊
     */
    @Transactional
    public HomeBlockDTO updateBlock(Long blockId, UpdateHomeBlockRequest request) {
        log.info("更新區塊,ID: {}", blockId);
        
        // 1. 檢查區塊是否存在
        HomeBlock block = homeBlockMapper.selectById(blockId);
        if (block == null) {
            throw new ResourceNotFoundException("區塊", blockId);
        }
        
        // 2. 驗證（如果有修改 type 或 productIds）
        String newType = request.getType() != null ? request.getType() : block.getType();
        validateBlockRequest(newType, request.getProductIds());
        
        // 3. 更新欄位
        if (request.getType() != null) block.setType(request.getType());
        if (request.getTitle() != null) block.setTitle(request.getTitle());
        if (request.getSubtitle() != null) block.setSubtitle(request.getSubtitle());
        if (request.getImageUrl() != null) block.setImageUrl(request.getImageUrl());
        if (request.getLinkUrl() != null) block.setLinkUrl(request.getLinkUrl());
        if (request.getSortOrder() != null) block.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) block.setIsActive(request.getIsActive());
        if (request.getStartTime() != null) {
            block.setStartTime(request.getStartTime().atOffset(java.time.ZoneOffset.UTC));
        }
        if (request.getEndTime() != null) {
            block.setEndTime(request.getEndTime().atOffset(java.time.ZoneOffset.UTC));
        }
        
        block.setBlockId(blockId); // for WHERE 條件
        
        // 4. 更新 home_block
        homeBlockMapper.updateHomeBlock(block);
        
        // 5. 更新商品關聯（如果提供了 productIds）
        if (request.getProductIds() != null) {
            // 先刪除舊的關聯
            homeBlockProductMapper.deleteByBlockId(blockId);
            
            // 再新增新的關聯
            if (!request.getProductIds().isEmpty()) {
                saveBlockProducts(blockId, request.getProductIds());
            }
        }
        
        log.info("區塊更新成功,ID: {}", blockId);
        
        return convertToDTO(homeBlockMapper.selectById(blockId));
    }
    
    /**
     * 刪除區塊
     */
    @Transactional
    public void deleteBlock(Long blockId) {
        log.info("刪除區塊,ID: {}", blockId);
        
        // 檢查區塊是否存在
        HomeBlock block = homeBlockMapper.selectById(blockId);
        if (block == null) {
            throw new ResourceNotFoundException("區塊", blockId);
        }
        
        // 刪除（關聯表會自動級聯刪除）
        homeBlockMapper.deleteById(blockId);
        
        log.info("區塊刪除成功,ID: {}", blockId);
    }
    
    /**
     * 驗證區塊請求
     */
    private void validateBlockRequest(String type, List<Long> productIds) {
        // 驗證類型
        if (type == null || type.trim().isEmpty()) {
            throw new BusinessException("區塊類型不可為空");
        }
        
        // 如果是 PRODUCT_RECOMMEND 類型，必須提供商品 ID
        if ("PRODUCT_RECOMMEND".equals(type)) {
            if (productIds == null || productIds.isEmpty()) {
                throw new BusinessException("商品推薦區塊必須指定至少一個商品");
            }
            
            // 驗證商品是否存在
            for (Long productId : productIds) {
                Product product = productMapper.selectProductById(productId);
                if (product == null) {
                    throw new ResourceNotFoundException("商品", productId);
                }
            }
        }
    }
    
    /**
     * 儲存區塊與商品的關聯
     */
    private void saveBlockProducts(Long blockId, List<Long> productIds) {
        List<HomeBlockProduct> relations = new ArrayList<>();
        
        for (int i = 0; i < productIds.size(); i++) {
            HomeBlockProduct relation = new HomeBlockProduct();
            relation.setBlockId(blockId);
            relation.setProductId(productIds.get(i));
            relation.setSortOrder(i); // 0=主圖, 1,2,...=子商品
            relations.add(relation);
        }
        
        if (!relations.isEmpty()) {
            homeBlockProductMapper.insertBatch(relations);
        }
    }
    
    /**
     * 將 Entity 轉換為 DTO
     */
    private HomeBlockDTO convertToDTO(HomeBlock block) {
        HomeBlockDTO dto = new HomeBlockDTO();
        dto.setBlockId(block.getBlockId());
        dto.setType(block.getType());
        dto.setTitle(block.getTitle());
        dto.setSubtitle(block.getSubtitle());
        dto.setImageUrl(block.getImageUrl());
        dto.setLinkUrl(block.getLinkUrl());
        dto.setSortOrder(block.getSortOrder());
        dto.setIsActive(block.getIsActive());
        // 轉換 OffsetDateTime 為 LocalDateTime
        if (block.getStartTime() != null) {
            dto.setStartTime(block.getStartTime().toLocalDateTime());
        }
        if (block.getEndTime() != null) {
            dto.setEndTime(block.getEndTime().toLocalDateTime());
        }
        
        // 如果是 PRODUCT_RECOMMEND 類型，載入關聯商品
        if ("PRODUCT_RECOMMEND".equals(block.getType())) {
            List<HomeBlockProduct> relations = homeBlockProductMapper.selectByBlockId(block.getBlockId());
            
            if (relations != null && !relations.isEmpty()) {
                List<ProductDTO> products = relations.stream()
                        .map(rel -> {
                            Product product = productMapper.selectProductById(rel.getProductId());
                            return product != null ? convertProductToDTO(product) : null;
                        })
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
                
                dto.setProducts(products);
            } else {
                dto.setProducts(Collections.emptyList());
            }
        }
        
        return dto;
    }
    
    /**
     * Product Entity 轉 DTO（簡化版）
     */
    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setCategoryId(product.getCategoryId());
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setNameEn(product.getNameEn());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setDescription(product.getDescription());
        dto.setIsNew(product.getIsNew());
        dto.setIsSoldOut(product.getIsSoldOut());
        dto.setIsActive(product.getIsActive());
        return dto;
    }
}
