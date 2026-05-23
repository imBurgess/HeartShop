package com.HeartShop.mapper;

import com.HeartShop.entity.Wishlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistMapper {

    List<Wishlist> findByMemberId(@Param("memberId") Long memberId);

    int existsByMemberAndProduct(@Param("memberId") Long memberId,
                                 @Param("productId") Long productId);

    void insert(@Param("memberId") Long memberId,
                @Param("productId") Long productId);

    int deleteByMemberAndProduct(@Param("memberId") Long memberId,
                                 @Param("productId") Long productId);
}
