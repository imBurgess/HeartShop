package com.HeartShop.service;

import com.HeartShop.dto.AddToCartRequest;
import com.HeartShop.dto.CartItemDTO;
import com.HeartShop.dto.UpdateCartRequest;
import com.HeartShop.entity.CartItem;
import com.HeartShop.entity.Product;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.mapper.CartMapper;
import com.HeartShop.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public List<CartItemDTO> getCartItems(Long memberId) {
        return cartMapper.selectCartItemsByMemberId(memberId);
    }

    @Transactional
    public void addToCart(Long memberId, AddToCartRequest request) {
        // Check if product exists
        Product product = productMapper.selectProductById(request.getProductId());
        if (product == null) {
            throw new BusinessException("4004", "商品不存在");
        }

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BusinessException("4000", "該商品已下架");
        }

        // Check if already in cart
        Optional<CartItem> existingItem = cartMapper.selectCartItemByProductAndSize(memberId, request.getProductId(), request.getSizeName());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            cartMapper.updateCartItemQuantity(item.getCartItemId(), item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setMemberId(memberId);
            newItem.setProductId(request.getProductId());
            newItem.setSizeName(request.getSizeName());
            newItem.setQuantity(request.getQuantity());
            cartMapper.insertCartItem(newItem);
        }
    }

    @Transactional
    public void updateQuantity(Long memberId, Long cartItemId, UpdateCartRequest request) {
        CartItem item = cartMapper.selectById(cartItemId)
                .orElseThrow(() -> new BusinessException("4004", "購物車項目不存在"));

        if (!item.getMemberId().equals(memberId)) {
            throw new BusinessException("4003", "無權限修改");
        }

        cartMapper.updateCartItemQuantity(cartItemId, request.getQuantity());
    }

    @Transactional
    public void removeCartItem(Long memberId, Long cartItemId) {
        int rows = cartMapper.deleteCartItem(cartItemId, memberId);
        if (rows == 0) {
            throw new BusinessException("4004", "項目不存在或無權限刪除");
        }
    }
    
    @Transactional
    public void clearCart(Long memberId) {
        cartMapper.deleteCartItemsByMemberId(memberId);
    }
}
