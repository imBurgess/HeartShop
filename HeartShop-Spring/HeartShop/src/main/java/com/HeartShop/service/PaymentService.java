package com.HeartShop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class PaymentService {

    private static final String HASH_KEY = "spPjZn66i0OhqJsQ"; // ECPay Test HashKey
    private static final String HASH_IV = "hT5OJckN45isQTTs";  // ECPay Test HashIV
    private static final String MERCHANT_ID = "3002599";       // ECPay Test MerchantID
    private static final String ECPAY_AIO_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";
    
    // Server webhook return URL (For local dev, usually requires ngrok, but we set a dummy one)
    private static final String RETURN_URL = "https://example.com/api/payment/ecpay/return";
    // Client return URL (Where to redirect user after payment)
    private static final String CLIENT_BACK_URL = "http://localhost:3000/cart/finoder";

    /**
     * 產生綠界 ECPay 的 HTML 表單字串
     */
    public String generateECPayHtml(String orderNo, int totalAmount, String itemName, String paymentMethod) {
        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", MERCHANT_ID);
        params.put("MerchantTradeNo", orderNo);
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount));
        params.put("TradeDesc", "HeartShop Order");
        params.put("ItemName", itemName != null && !itemName.isEmpty() ? itemName : "HeartShop Products");
        params.put("ReturnURL", RETURN_URL);
        // Add orderNo to ClientBackURL so the frontend knows which order it is
        params.put("ClientBackURL", CLIENT_BACK_URL + "?orderNo=" + orderNo);
        params.put("EncryptType", "1");

        // 依據選擇的付款方式決定綠界付款頁面預設顯示方式
        if ("credit_card".equals(paymentMethod)) {
            params.put("ChoosePayment", "Credit");
        } else {
            // Line Pay 或其他
            params.put("ChoosePayment", "ALL");
        }

        // 產生 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 產生 HTML form
        StringBuilder html = new StringBuilder();
        html.append("<form id=\"ecpay-form\" action=\"").append(ECPAY_AIO_URL).append("\" method=\"POST\">");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type=\"hidden\" name=\"").append(entry.getKey()).append("\" value=\"").append(entry.getValue()).append("\">");
        }
        html.append("</form>");
        html.append("<script>document.getElementById('ecpay-form').submit();</script>");

        return html.toString();
    }

    /**
     * 驗證綠界回傳的 CheckMacValue
     */
    public boolean verifyCheckMacValue(Map<String, String> params) {
        String receivedMac = params.get("CheckMacValue");
        if (receivedMac == null) return false;

        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("CheckMacValue"); // 不包含 CheckMacValue 本身

        String calculatedMac = generateCheckMacValue(verifyParams);
        return calculatedMac.equals(receivedMac);
    }

    private String generateCheckMacValue(Map<String, String> params) {
        // 1. 依字母順序排序 (TreeMap 自動 A~Z)
        TreeMap<String, String> sorted = new TreeMap<>(params);
        sorted.remove("CheckMacValue");

        // 2. 串接成 HashKey=...&K=V&...&HashIV=...
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(HASH_KEY);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        sb.append("&HashIV=").append(HASH_IV);

        // 3. URL Encode 整串後轉小寫
        try {
            String urlEncoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8)
                    .toLowerCase();

            // 4. SHA256 → 轉大寫
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(urlEncoded.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception e) {
            log.error("CheckMacValue 產生失敗", e);
            return "";
        }
    }
}
