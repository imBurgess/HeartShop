package com.HeartShop.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Wishlist {
    private Long wishlistId;
    private Long memberId;
    private Long productId;
    private Date createdAt;

    // Joined from product / product_image
    private String productName;
    private String productNameEn;
    private String productCode;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String productImage;
}
