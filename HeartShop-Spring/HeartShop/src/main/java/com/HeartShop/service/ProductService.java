package com.HeartShop.service;

import com.HeartShop.dto.CreateProductRequest;
import com.HeartShop.dto.ProductDTO;
import com.HeartShop.dto.ProductQueryParams;
import com.HeartShop.dto.UpdateProductRequest;
import com.HeartShop.entity.Category;
import com.HeartShop.entity.Product;
import com.HeartShop.entity.ProductImage;
import com.HeartShop.entity.InventoryLog;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.exception.ResourceNotFoundException;
import com.HeartShop.mapper.CategoryMapper;
import com.HeartShop.mapper.InventoryLogMapper;
import com.HeartShop.mapper.ProductMapper;
import com.HeartShop.mapper.ProductImageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品 Service
 */
@Slf4j
@Service
public class ProductService {
    
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;
    private final InventoryLogMapper inventoryLogMapper;

    public ProductService(ProductMapper productMapper, CategoryMapper categoryMapper,
                          ProductImageMapper productImageMapper, InventoryLogMapper inventoryLogMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productImageMapper = productImageMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }
    
    /**
     * 查詢商品列表
     */
    public Map<String, Object> getProducts(ProductQueryParams params) {
        log.debug("查詢商品列表,參數: {}", params);
        
        // 驗證並調整分頁參數
        int page = (params.getPage() != null && params.getPage() > 0) ? params.getPage() : 1;
        int pageSize = (params.getPageSize() != null && params.getPageSize() > 0) ? params.getPageSize() : 20;
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每頁筆數
        }
        int offset = (page - 1) * pageSize;
        
        // 查詢商品
        List<Product> products = productMapper.selectProducts(
            params.getCategoryId(),
            params.getIsActive(),
            params.getIsNew(),
            params.getKeyword(),
            params.getSortBy(),
            params.getSortOrder(),
            pageSize,
            offset
        );
        
        // 查詢總數
        int total = productMapper.countProducts(
            params.getCategoryId(),
            params.getIsActive(),
            params.getIsNew(),
            params.getKeyword()
        );
        
        // 轉換為 DTO
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 組裝回傳格式
        Map<String, Object> result = new HashMap<>();
        result.put("items", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        
        return result;
    }
    
    /**
     * 根據 ID 查詢商品
     */
    public ProductDTO getProductById(Long id) {
        log.debug("查詢商品,ID: {}", id);
        
        Product product = productMapper.selectProductById(id);
        if (product == null) {
            throw new ResourceNotFoundException("商品", id);
        }
        
        // 增加瀏覽次數
        productMapper.incrementViewCount(id);
        
        return convertToDTO(product);
    }
    
    /**
     * 根據商品編號查詢
     */
    public ProductDTO getProductByCode(String code) {
        log.debug("查詢商品,編號: {}", code);
        
        Product product = productMapper.selectProductByCode(code);
        if (product == null) {
            throw new ResourceNotFoundException("商品編號不存在: " + code);
        }
        
        return convertToDTO(product);
    }
    
    /**
     * 建立商品
     */
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("建立商品: {}", request.getName());
        
        // 1. 驗證分類是否存在
        Category category = categoryMapper.selectCategoryById(request.getCategoryId());
        if (category == null) {
            throw new ResourceNotFoundException("分類", request.getCategoryId());
        }
        
        // 2. 處理商品編號 (若未提供則自動生成)
        String productCode = request.getCode();
        if (productCode == null || productCode.trim().isEmpty()) {
             // 生成格式: P + yyyyMMddHHmmss + 3位亂數
             productCode = "P" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                         + String.format("%03d", new java.util.Random().nextInt(1000));
        }
        
        if (productMapper.countByCode(productCode, null) > 0) {
            throw new BusinessException("商品編號已存在: " + productCode);
        }
        
        // 3. 建立 Product Entity
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setCode(productCode);
        product.setName(request.getName());
        product.setNameEn(request.getNameEn());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setDescription(request.getDescription());
        product.setSizeInfo(request.getSizeInfo());
        
        // 處理 tags（List -> String）
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            product.setTags(String.join(",", request.getTags()));
        }
        
        product.setIsNew(request.getIsNew() != null ? request.getIsNew() : false);
        product.setIsSoldOut(false);  // 新商品預設未售完
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setViewCount(0);
        product.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 0;
        product.setStockQuantity(initialStock);

        // 4. 儲存商品
        productMapper.insertProduct(product);

        // 4a. 記錄初始庫存
        if (initialStock > 0) {
            InventoryLog log = new InventoryLog();
            log.setProductId(product.getProductId());
            log.setChangeType("INITIAL");
            log.setQuantityBefore(0);
            log.setQuantityChange(initialStock);
            log.setQuantityAfter(initialStock);
            log.setOperator("admin");
            log.setRemark("商品建立初始庫存");
            inventoryLogMapper.insertLog(log);
        }

        // 5. 儲存圖片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveImages(product.getProductId(), request.getImages());
        }
        
        log.info("商品建立成功,ID: {}, 名稱: {}", product.getProductId(), product.getName());
        
        return convertToDTO(product);
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        log.info("更新商品,ID: {}", id);
        
        // 1. 檢查商品是否存在
        Product product = productMapper.selectProductById(id);
        if (product == null) {
            throw new ResourceNotFoundException("商品", id);
        }
        
        // 2. 檢查分類
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectCategoryById(request.getCategoryId());
            if (category == null) {
                throw new ResourceNotFoundException("分類", request.getCategoryId());
            }
            product.setCategoryId(request.getCategoryId());
        }
        
        // 3. 檢查商品編號
        if (request.getCode() != null) {
            if (productMapper.countByCode(request.getCode(), id) > 0) {
                throw new BusinessException("商品編號已存在: " + request.getCode());
            }
            product.setCode(request.getCode());
        }
        
        // 4. 更新其他欄位
        if (request.getName() != null) product.setName(request.getName());
        if (request.getNameEn() != null) product.setNameEn(request.getNameEn());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getSizeInfo() != null) product.setSizeInfo(request.getSizeInfo());
        
        // 處理 tags
        if (request.getTags() != null) {
            if (request.getTags().isEmpty()) {
                product.setTags(null);
            } else {
                product.setTags(String.join(",", request.getTags()));
            }
        }
        
        if (request.getIsNew() != null) product.setIsNew(request.getIsNew());
        if (request.getIsSoldOut() != null) product.setIsSoldOut(request.getIsSoldOut());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getSortOrder() != null) product.setSortOrder(request.getSortOrder());
        
        // 5. 設定 productId（for MyBatis WHERE 條件）
        product.setProductId(id);
        
        // 6. 更新商品
        productMapper.updateProduct(product);

        // 6a. 若有傳入 stock，調整庫存並記錄
        if (request.getStock() != null) {
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int newStock = request.getStock();
            if (newStock != currentStock) {
                productMapper.updateStock(id, newStock);
                InventoryLog log = new InventoryLog();
                log.setProductId(id);
                log.setChangeType("ADJUST");
                log.setQuantityBefore(currentStock);
                log.setQuantityChange(newStock - currentStock);
                log.setQuantityAfter(newStock);
                log.setOperator("admin");
                log.setRemark("商品編輯調整庫存");
                inventoryLogMapper.insertLog(log);
            }
        }

        // 7. 更新圖片 (先刪後加，簡單處理)
        if (request.getImages() != null) {
            productImageMapper.deleteByProductId(id);
            if (!request.getImages().isEmpty()) {
                saveImages(id, request.getImages());
            }
        }
        
        log.info("商品更新成功,ID: {}", id);
        
        // 重新查詢以取得最新資料
        return convertToDTO(productMapper.selectProductById(id));
    }
    
    /**
     * 刪除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("刪除商品,ID: {}", id);
        
        // 檢查商品是否存在
        Product product = productMapper.selectProductById(id);
        if (product == null) {
            throw new ResourceNotFoundException("商品", id);
        }
        
        // 刪除關聯圖片
        productImageMapper.deleteByProductId(id);
        
        productMapper.deleteProduct(id);
        
        log.info("商品刪除成功,ID: {}", id);
    }
    
    /**
     * 儲存圖片到資料庫
     */
    private void saveImages(Long productId, List<String> imageUrls) {
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage img = new ProductImage();
            img.setProductId(productId);
            img.setImageUrl(imageUrls.get(i));
            img.setSortOrder(i);
            images.add(img);
        }
        productImageMapper.insertBatch(images);
    }
    
    /**
     * 將 Entity 轉換為 DTO（公開方法供其他 Service 使用）
     */
    public ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setCategoryId(product.getCategoryId());
        
        // 查詢分類名稱
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectCategoryById(product.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getNameZh());
            }
        }
        
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setNameEn(product.getNameEn());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setDescription(product.getDescription());
        dto.setSizeInfo(product.getSizeInfo());
        
        // 處理 tags（String -> List）
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(Arrays.asList(product.getTags().split(",")));
        } else {
            dto.setTags(Collections.emptyList());
        }
        
        dto.setIsNew(product.getIsNew());
        dto.setIsSoldOut(product.getIsSoldOut());
        dto.setIsActive(product.getIsActive());
        dto.setViewCount(product.getViewCount());
        dto.setSortOrder(product.getSortOrder());
        
        // 庫存資訊
        dto.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
        dto.setStockAlertThreshold(product.getStockAlertThreshold() != null ? product.getStockAlertThreshold() : 10);
        
        // 判斷是否低庫存
        Integer stock = dto.getStockQuantity();
        Integer threshold = dto.getStockAlertThreshold();
        dto.setIsLowStock(stock <= threshold);
        
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // 讀取圖片 URLs
        List<ProductImage> productImages = productImageMapper.selectByProductId(product.getProductId());
        List<String> imageUrls = productImages.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImages(imageUrls);
        
        // 設定主要圖片 (for Frontend)
        if (!imageUrls.isEmpty()) {
            dto.setImageUrl(imageUrls.get(0));
        }
        
        return dto;
    }
}
