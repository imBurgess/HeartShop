package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {
    private String orderNo;
    private String paymentUrl;
    private Map<String, String> ecpayParams; // 用來放要 POST 給綠界的表單參數
}
