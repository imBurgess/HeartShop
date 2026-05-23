package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.Wishlist;
import com.HeartShop.mapper.WishlistMapper;
import com.HeartShop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistMapper wishlistMapper;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<Wishlist>> getWishlist(
            @RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        return ApiResponse.success(wishlistMapper.findByMemberId(memberId));
    }

    @GetMapping("/{productId}/status")
    public ApiResponse<Map<String, Boolean>> checkStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        boolean wishlisted = wishlistMapper.existsByMemberAndProduct(memberId, productId) > 0;
        return ApiResponse.success(Map.of("wishlisted", wishlisted));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Void> addToWishlist(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        wishlistMapper.insert(memberId, productId);
        return ApiResponse.success("已加入收藏", null);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> removeFromWishlist(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        wishlistMapper.deleteByMemberAndProduct(memberId, productId);
        return ApiResponse.success("已取消收藏", null);
    }
}
