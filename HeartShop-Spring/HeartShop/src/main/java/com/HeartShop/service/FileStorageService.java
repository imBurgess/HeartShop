package com.HeartShop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    // 儲存根目錄
    private final Path fileStorageLocation;

    public FileStorageService() {
        // 預設儲存在專案執行目錄下的 uploads
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        
        log.info("初始化 FileStorageService，上傳目錄: {}", this.fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("上傳目錄建立成功或已存在");
        } catch (Exception ex) {
            log.error("無法建立上傳目錄: {}", this.fileStorageLocation, ex);
            throw new RuntimeException("無法建立上傳目錄", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        log.debug("開始儲存檔案，原始檔名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // 檢查檔名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.contains("..")) {
            log.warn("無效的檔案名稱: {}", originalFileName);
            throw new RuntimeException("無效的檔案名稱: " + originalFileName);
        }

        // 產生新檔名: yyyyMMdd_UUID.ext
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = originalFileName.substring(dotIndex);
        }
        
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String newFileName = dateStr + "_" + UUID.randomUUID().toString() + ext;
        
        log.debug("新檔名: {}", newFileName);

        try {
            // 複製檔案到目標位置 (取代同名檔案)
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            log.debug("目標路徑: {}", targetLocation);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("檔案儲存成功: {}", newFileName);

            // 回傳相對路徑 (給前端訪問用)
            // 注意：這裡回傳的是 URL 路徑，不是檔案系統路徑
            // 因為設定了 context-path=/api，所以需要包含 /api 前綴
            return "/api/uploads/" + newFileName;
        } catch (IOException ex) {
            log.error("無法儲存檔案 {}", newFileName, ex);
            throw new RuntimeException("無法儲存檔案 " + newFileName, ex);
        }
    }
}
