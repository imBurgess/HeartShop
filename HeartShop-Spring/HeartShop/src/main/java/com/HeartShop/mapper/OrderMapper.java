package com.HeartShop.mapper;

import com.HeartShop.entity.Order;
import com.HeartShop.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    
    void insertOrderItems(@Param("items") List<OrderItem> items);
    
    Order findByOrderNo(@Param("orderNo") String orderNo);
    
    void updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") String status);

    List<Order> findByMemberId(@Param("memberId") Long memberId);

    Order findByOrderNoSimple(@Param("orderNo") String orderNo);

    int cancelOrderForMember(@Param("orderNo") String orderNo,
                             @Param("memberId") Long memberId);

    // Admin
    List<Order> adminFindAll(@Param("keyword") String keyword,
                             @Param("status") String status,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    long adminCountAll(@Param("keyword") String keyword,
                       @Param("status") String status,
                       @Param("startDate") String startDate,
                       @Param("endDate") String endDate);

    void adminUpdateStatus(@Param("orderNo") String orderNo,
                           @Param("status") String status);

    int adminDeleteOrder(@Param("orderNo") String orderNo);
}
