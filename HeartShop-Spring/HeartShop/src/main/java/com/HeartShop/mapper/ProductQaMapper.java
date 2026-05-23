package com.HeartShop.mapper;

import com.HeartShop.entity.ProductQa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductQaMapper {

    List<ProductQa> findByProductId(@Param("productId") Long productId);

    List<ProductQa> findByMemberId(@Param("memberId") Long memberId);

    int insert(ProductQa qa);

    // ── Admin ──────────────────────────────────────
    List<ProductQa> adminFindAll(@Param("answered") Boolean answered,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    long adminCountAll(@Param("answered") Boolean answered);

    int updateAnswer(@Param("qaId") Long qaId, @Param("answer") String answer);

    ProductQa findById(@Param("qaId") Long qaId);

    int deleteById(@Param("qaId") Long qaId);
}
