package com.HeartShop.mapper;

import com.HeartShop.dto.CartItemDTO;
import com.HeartShop.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CartMapper {
    List<CartItemDTO> selectCartItemsByMemberId(@Param("memberId") Long memberId);
    Optional<CartItem> selectCartItemByProductAndSize(@Param("memberId") Long memberId, @Param("productId") Long productId, @Param("sizeName") String sizeName);
    Optional<CartItem> selectById(@Param("cartItemId") Long cartItemId);
    int insertCartItem(CartItem cartItem);
    int updateCartItemQuantity(@Param("cartItemId") Long cartItemId, @Param("quantity") Integer quantity);
    int deleteCartItem(@Param("cartItemId") Long cartItemId, @Param("memberId") Long memberId);
    int deleteCartItemsByMemberId(@Param("memberId") Long memberId);
}
