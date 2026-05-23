package com.HeartShop.service;

import com.HeartShop.dto.CartItemDTO;
import com.HeartShop.dto.PlaceOrderRequest;
import com.HeartShop.dto.PlaceOrderResponse;
import com.HeartShop.entity.Order;
import com.HeartShop.entity.OrderItem;
import com.HeartShop.mapper.CartMapper;
import com.HeartShop.mapper.OrderMapper;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.exception.ResourceNotFoundException;
import com.HeartShop.utils.ECPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    
    @Value("${app.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public PlaceOrderResponse placeOrder(Long memberId, PlaceOrderRequest request) {
        // 1. 取得購物車商品
        List<CartItemDTO> cartItems = cartMapper.selectCartItemsByMemberId(memberId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("購物車為空，無法結帳");
        }

        // 2. 計算總金額
        int subtotalAmount = 0;
        for (CartItemDTO item : cartItems) {
            int price = item.getPrice() != null ? item.getPrice().intValue() : 0;
            subtotalAmount += price * item.getQuantity();
        }
        
        int shippingFee = 0;
        int discountAmount = 0;
        int bonusUsed = 0;
        int totalAmount = subtotalAmount + shippingFee - discountAmount - bonusUsed;

        if (totalAmount <= 0) {
            throw new RuntimeException("訂單總金額必須大於 0 才能使用綠界結帳");
        }

        // 3. 產生訂單編號
        String orderNo = generateOrderNo();

        // 4. 建立訂單主檔
        Order order = Order.builder()
                .orderNo(orderNo)
                .memberId(memberId)
                .subtotalAmount(subtotalAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .bonusUsed(bonusUsed)
                .totalAmount(totalAmount)
                .status("pending") // 未付款
                .paymentMethod(request.getPaymentMethod())
                .shippingMethod(request.getShippingMethod())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .remark(request.getReceiverNote())
                .build();
        
        orderMapper.insertOrder(order);

        // 5. 建立訂單明細
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            int unitPrice = item.getPrice() != null ? item.getPrice().intValue() : 0;
            int subtotal = unitPrice * item.getQuantity();
            return OrderItem.builder()
                .orderId(order.getOrderId())
                .productId(item.getProductId())
                .productCode("P" + item.getProductId()) // 簡化為 P + productId
                .productName(item.getName())
                .productImage(item.getImage())
                .sizeName(item.getSizeName())
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
        }).collect(Collectors.toList());

        orderMapper.insertOrderItems(orderItems);

        // 6. 清空購物車
        cartMapper.deleteCartItemsByMemberId(memberId);

        // 7. 產生綠界結帳表單參數
        String itemName = cartItems.stream()
                .map(CartItemDTO::getName)
                .collect(Collectors.joining("#"));
        // 綠界 ItemName 有長度限制，若過長則截斷
        if (itemName.length() > 100) {
            itemName = itemName.substring(0, 97) + "...";
        }

        // ReturnURL：後端 webhook（需公開 IP，本機測試無法接收）
        String returnUrl = serverUrl + "/api/orders/ecpay/notify";

        // ClientBackURL：消費者點「返回商店」時的跳轉頁
        String baseClientBackUrl = (request.getClientBackUrl() != null && !request.getClientBackUrl().isBlank())
                ? request.getClientBackUrl()
                : frontendUrl + "/cart/checkout";
        String finalClientBackUrl = baseClientBackUrl
                + (baseClientBackUrl.contains("?") ? "&" : "?")
                + "orderNo=" + orderNo;

        // OrderResultURL：付款完成後由瀏覽器 POST 到後端（本機可接收），用於即時更新狀態
        String orderResultUrl = serverUrl + "/api/orders/ecpay/result";

        Map<String, String> ecpayParams = ECPayUtil.generateOrderParams(
                orderNo, totalAmount, itemName, returnUrl, finalClientBackUrl, orderResultUrl
        );

        return PlaceOrderResponse.builder()
                .orderNo(orderNo)
                .paymentUrl(ECPayUtil.ECPAY_URL)
                .ecpayParams(ecpayParams)
                .build();
    }

    @Transactional(readOnly = true)
    public Order getOrderByNo(String orderNo, Long memberId) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new ResourceNotFoundException("找不到訂單：" + orderNo);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException("4003", "無權限存取此訂單");
        }
        return order;
    }

    @Transactional
    public void handleECPayNotify(Map<String, String> params) {
        // 取得綠界傳來的 CheckMacValue
        String receivedMac = params.get("CheckMacValue");
        
        // 使用我們自己的 Util 再次計算 (注意 ECPayUtil 會過濾掉 CheckMacValue)
        String calculatedMac = ECPayUtil.generateCheckMacValue(params, ECPayUtil.HASH_KEY, ECPayUtil.HASH_IV);
        
        if (!calculatedMac.equals(receivedMac)) {
            log.error("綠界 CheckMacValue 驗證失敗! Received: {}, Calculated: {}", receivedMac, calculatedMac);
            return;
        }

        // 驗證成功，判斷付款狀態 (RtnCode == 1 代表成功)
        String rtnCode = params.get("RtnCode");
        String merchantTradeNo = params.get("MerchantTradeNo");
        
        if ("1".equals(rtnCode)) {
            // 更新狀態為已付款
            orderMapper.updateOrderStatus(merchantTradeNo, "PAID");
            log.info("訂單 {} 付款成功，已更新狀態為 PAID", merchantTradeNo);
        } else {
            // 付款失敗
            orderMapper.updateOrderStatus(merchantTradeNo, "FAILED");
            log.info("訂單 {} 付款失敗，RtnCode: {}, RtnMsg: {}", merchantTradeNo, rtnCode, params.get("RtnMsg"));
        }
    }

    /** OrderResultURL：由使用者瀏覽器 POST，可在本機接收，即時更新付款狀態後導回前端 */
    @Transactional
    public String handleECPayResult(Map<String, String> params) {
        String orderNo = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode");
        String receivedMac = params.get("CheckMacValue");
        String calculatedMac = ECPayUtil.generateCheckMacValue(params, ECPayUtil.HASH_KEY, ECPayUtil.HASH_IV);

        if (!calculatedMac.equalsIgnoreCase(receivedMac)) {
            log.warn("OrderResult CheckMacValue 驗證失敗 orderNo={}", orderNo);
            return orderNo;
        }

        if ("1".equals(rtnCode)) {
            orderMapper.updateOrderStatus(orderNo, "PAID");
            log.info("訂單 {} 付款成功（OrderResultURL）", orderNo);
        } else {
            orderMapper.updateOrderStatus(orderNo, "FAILED");
            log.info("訂單 {} 付款失敗 RtnCode={}", orderNo, rtnCode);
        }
        return orderNo;
    }

    @Transactional(readOnly = true)
    public List<Order> getMemberOrders(Long memberId) {
        return orderMapper.findByMemberId(memberId);
    }

    @Transactional
    public void cancelOrder(String orderNo, Long memberId) {
        int updated = orderMapper.cancelOrderForMember(orderNo, memberId);
        if (updated == 0) {
            Order order = orderMapper.findByOrderNoSimple(orderNo);
            if (order == null) throw new ResourceNotFoundException("找不到訂單：" + orderNo);
            if (!order.getMemberId().equals(memberId)) throw new BusinessException("4003", "無權限操作此訂單");
            throw new BusinessException("4002", "此訂單狀態無法取消（目前狀態：" + order.getStatus() + "）");
        }
    }

    @Transactional(readOnly = true)
    public PlaceOrderResponse repayOrder(String orderNo, Long memberId) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) throw new ResourceNotFoundException("找不到訂單：" + orderNo);
        if (!order.getMemberId().equals(memberId)) throw new BusinessException("4003", "無權限操作此訂單");
        if (!"pending".equals(order.getStatus())) throw new BusinessException("4002", "此訂單不是待付款狀態");

        String itemName = order.getItems().stream()
                .map(i -> i.getProductName())
                .collect(Collectors.joining("#"));
        if (itemName.length() > 100) itemName = itemName.substring(0, 97) + "...";

        String returnUrl = serverUrl + "/api/orders/ecpay/notify";
        String clientBackUrl = frontendUrl + "/cart/checkout?orderNo=" + orderNo;
        String orderResultUrl = serverUrl + "/api/orders/ecpay/result";

        Map<String, String> ecpayParams = ECPayUtil.generateOrderParams(
                orderNo, order.getTotalAmount(), itemName, returnUrl, clientBackUrl, orderResultUrl
        );

        return PlaceOrderResponse.builder()
                .orderNo(orderNo)
                .paymentUrl(ECPayUtil.ECPAY_URL)
                .ecpayParams(ecpayParams)
                .build();
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        int randomNum = (int) (Math.random() * 9000) + 1000;
        return "ORD" + sdf.format(new Date()) + randomNum;
    }
}
