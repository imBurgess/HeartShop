package com.HeartShop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotBlank(message = "收件人姓名不可為空")
    private String receiverName;
    
    @NotBlank(message = "收件人電話不可為空")
    private String receiverPhone;
    
    @NotBlank(message = "收件地址不可為空")
    private String receiverAddress;
    
    private String receiverNote;
    
    @NotBlank(message = "付款方式不可為空")
    private String paymentMethod; // e.g., ecpay, credit_card
    
    @NotBlank(message = "物流方式不可為空")
    private String shippingMethod; // e.g., home
    
    // 將訂單完成後的返回網址交由前端決定，後端整合到 ECPay 表單
    private String clientBackUrl;
}
