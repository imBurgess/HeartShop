package com.HeartShop.exception;
import com.HeartShop.common.ResponseCode;
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(
                ResponseCode.NOT_FOUND.getCode(),
                String.format("%s (ID: %d) 不存在", resourceName, id)
        );
    }

    public ResourceNotFoundException(String message) {
        super(ResponseCode.NOT_FOUND.getCode(), message);
    }
}