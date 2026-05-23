package com.HeartShop.exception;
import com.HeartShop.common.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public  BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public BusinessException(String message){
        super(message);
        this.code = ResponseCode.INTERNAL_SERVER_ERROR.getCode();
    }
}
