package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.PlaceOrderRequest;
import com.HeartShop.dto.PlaceOrderResponse;
import com.HeartShop.entity.Order;
import com.HeartShop.service.OrderService;
import com.HeartShop.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ApiResponse<PlaceOrderResponse> placeOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PlaceOrderRequest request) {
        
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        PlaceOrderResponse response = orderService.placeOrder(memberId, request);
        return ApiResponse.success("訂單建立成功", response);
    }

    @GetMapping("/member")
    public ApiResponse<java.util.List<Order>> getMemberOrders(
            @RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        return ApiResponse.success("查詢成功", orderService.getMemberOrders(memberId));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
        
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        Order order = orderService.getOrderByNo(orderNo, memberId);
        return ApiResponse.success("查詢訂單成功", order);
    }

    @PatchMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        orderService.cancelOrder(orderNo, memberId);
        return ApiResponse.success("訂單已取消", null);
    }

    @PostMapping("/{orderNo}/repay")
    public ApiResponse<PlaceOrderResponse> repayOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        PlaceOrderResponse response = orderService.repayOrder(orderNo, memberId);
        return ApiResponse.success("重新付款參數已產生", response);
    }

    /**
     * 接收綠界金流伺服器端 Notify（需公開 IP，本機測試無法收到）
     */
    @PostMapping("/ecpay/notify")
    public ResponseEntity<String> ecpayNotify(@RequestParam Map<String, String> allParams) {
        log.info("收到綠界付款通知: {}", allParams);
        try {
            orderService.handleECPayNotify(allParams);
            return ResponseEntity.ok("1|OK");
        } catch (Exception e) {
            log.error("處理綠界通知時發生錯誤", e);
            return ResponseEntity.status(500).body("0|Error");
        }
    }

    /**
     * OrderResultURL：付款完成後由使用者瀏覽器 POST，本機可接收
     * 驗證付款結果 → 更新訂單狀態 → 導回前端完成頁
     */
    @PostMapping("/ecpay/result")
    public void ecpayResult(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        String orderNo = allParams.get("MerchantTradeNo");
        log.info("收到綠界 OrderResult orderNo={}", orderNo);

        try {
            orderNo = orderService.handleECPayResult(allParams);
        } catch (Exception e) {
            log.error("處理 OrderResult 發生錯誤", e);
        }

        String redirectUrl = orderService.getFrontendUrl()
                + "/cart/checkout?orderNo=" + (orderNo != null ? orderNo : "");
        response.sendRedirect(redirectUrl);
    }
}
