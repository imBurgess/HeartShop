package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")  // 移除 /api，因為 context-path 已包含
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上傳圖片
     * POST /api/upload/image
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("接收到檔案上傳請求: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return ApiResponse.error("400", "上傳檔案不能為空");
        }
        
        // 檢查是否為圖片 (簡單檢查 contentType)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error("400", "只允許上傳圖片檔案");
        }

        try {
            String fileUrl = fileStorageService.storeFile(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            
            return ApiResponse.success("上傳成功", result);
        } catch (Exception e) {
            log.error("檔案上傳失敗", e);
            return ApiResponse.error("500", "檔案上傳失敗: " + e.getMessage());
        }
    }
}
