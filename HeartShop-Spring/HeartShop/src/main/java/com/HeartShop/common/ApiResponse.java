package com.HeartShop.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                data,
                LocalDateTime.now()
        );
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                ResponseCode.SUCCESS.getCode(),
                message,
                data,
                LocalDateTime.now()
        );
    }
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(
                code,
                message,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return error(responseCode.getCode(), responseCode.getMessage());
    }
}
