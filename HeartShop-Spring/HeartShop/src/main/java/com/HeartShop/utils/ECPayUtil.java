package com.HeartShop.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class ECPayUtil {

    // 綠界測試環境金鑰 (特店編號 3002599)
    public static final String HASH_KEY = "spPjZn66i0OhqJsQ";
    public static final String HASH_IV = "hT5OJckN45isQTTs";
    public static final String MERCHANT_ID = "3002599";
    public static final String ECPAY_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    /**
     * 產生綠界結帳所需的所有表單參數
     */
    public static Map<String, String> generateOrderParams(
            String orderNo,
            int totalAmount,
            String itemName,
            String returnUrl,
            String clientBackUrl,
            String orderResultUrl) {

        Map<String, String> params = new TreeMap<>();
        
        // 基本參數
        params.put("MerchantID", MERCHANT_ID);
        params.put("MerchantTradeNo", orderNo);
        
        // 格式化日期：yyyy/MM/dd HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        params.put("MerchantTradeDate", sdf.format(new Date()));
        
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount));
        params.put("TradeDesc", "HeartShop Order");
        params.put("ItemName", itemName);
        params.put("ReturnURL", returnUrl);       // Server 端接收付款結果
        params.put("ClientBackURL", clientBackUrl); // 消費者點擊返回商店的按鈕，或是付款完成後導回的頁面
        // 移除 OrderResultURL，讓綠界顯示預設的付款成功畫面，並由使用者手動點擊返回商店
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1"); // SHA256
        // OrderResultURL：付款完成後瀏覽器 POST 到後端，本機可接收（優先於 ClientBackURL）
        if (orderResultUrl != null && !orderResultUrl.isBlank()) {
            params.put("OrderResultURL", orderResultUrl);
        }

        // 產生 CheckMacValue
        String checkMacValue = generateCheckMacValue(params, HASH_KEY, HASH_IV);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    public static String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        try {
            // 1. 參數按字母排序
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            sortedParams.remove("CheckMacValue");

            // 2. 串接字串
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(hashKey).append("&");
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.append("HashIV=").append(hashIv);

            // 3. URL Encode 整串後轉小寫
            String urlEncoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8)
                    .toLowerCase();

            // 4. SHA256 Hash → 轉大寫
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(urlEncoded.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02X", b));
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue 產生失敗", e);
        }
    }
}
