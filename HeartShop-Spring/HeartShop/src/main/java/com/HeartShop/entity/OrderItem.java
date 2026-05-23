package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productCode;
    private String productName;
    private String productImage;
    private String sizeName;
    private Integer unitPrice;
    private Integer quantity;
    private Integer subtotal;
    private Date createdAt;
}
