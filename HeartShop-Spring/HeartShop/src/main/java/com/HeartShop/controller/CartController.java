package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.AddToCartRequest;
import com.HeartShop.dto.CartItemDTO;
import com.HeartShop.dto.UpdateCartRequest;
import com.HeartShop.service.CartService;
import com.HeartShop.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<CartItemDTO>> getCart(@RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        return ApiResponse.success(cartService.getCartItems(memberId));
    }

    @PostMapping
    public ApiResponse<Void> addToCart(@RequestHeader("Authorization") String token,
                                       @Valid @RequestBody AddToCartRequest request) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        cartService.addToCart(memberId, request);
        return ApiResponse.success("加入購物車成功", null);
    }

    @PutMapping("/{cartItemId}")
    public ApiResponse<Void> updateCartItem(@RequestHeader("Authorization") String token,
                                            @PathVariable Long cartItemId,
                                            @Valid @RequestBody UpdateCartRequest request) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        cartService.updateQuantity(memberId, cartItemId, request);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{cartItemId}")
    public ApiResponse<Void> removeCartItem(@RequestHeader("Authorization") String token,
                                            @PathVariable Long cartItemId) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        cartService.removeCartItem(memberId, cartItemId);
        return ApiResponse.success("刪除成功", null);
    }
    
    @DeleteMapping
    public ApiResponse<Void> clearCart(@RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        cartService.clearCart(memberId);
        return ApiResponse.success("購物車已清空", null);
    }
}
