package com.HeartShop.mapper;

import com.HeartShop.entity.OrderQa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderQaMapper {

    List<OrderQa> findByOrderNo(@Param("orderNo") String orderNo);

    List<OrderQa> findByMemberId(@Param("memberId") Long memberId);

    int insert(OrderQa qa);

    // ── Admin ──────────────────────────────────────
    List<OrderQa> adminFindAll(@Param("answered") Boolean answered,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    long adminCountAll(@Param("answered") Boolean answered);

    int updateAnswer(@Param("qaId") Long qaId, @Param("answer") String answer);
}
