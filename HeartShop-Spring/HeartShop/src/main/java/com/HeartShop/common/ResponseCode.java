package com.HeartShop.common;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS("0000", "操作成功"),

    BAD_REQUEST("4000", "請求參數錯誤"),
    UNAUTHORIZED("4001", "未授權"),
    FORBIDDEN("4003", "無權限"),
    NOT_FOUND("4004", "資源不存在"),
    CONFLICT("4009", "資源衝突"),
    VALIDATION_ERROR("4022", "驗證錯誤"),

    INTERNAL_SERVER_ERROR("5000", "伺服器內部錯誤"),
    DATABASE_ERROR("5001", "資料庫錯誤"),
    SERVICE_UNAVAILABLE("5003", "服務暫時無法使用");
    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
