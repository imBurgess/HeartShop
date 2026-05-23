package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private Date orderDate;
    private Date shipDate;
    private String status; // pending, PAID, FAILED, COMPLETED
    private String paymentMethod;
    private String shippingMethod;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer subtotalAmount;
    private Integer shippingFee;
    private Integer discountAmount;
    private Integer bonusUsed;
    private Integer totalAmount;
    private String remark;
    private Date createdAt;
    private Date updatedAt;
    
    // 關聯的訂單項目
    private List<OrderItem> items;
}
