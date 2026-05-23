package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.Order;
import com.HeartShop.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderMapper orderMapper;

    @GetMapping
    public ApiResponse<Map<String, Object>> listOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        int offset = (page - 1) * pageSize;
        List<Order> items = orderMapper.adminFindAll(keyword, status, startDate, endDate, offset, pageSize);
        long total = orderMapper.adminCountAll(keyword, status, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return ApiResponse.success(result);
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrderDetail(@PathVariable String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            return ApiResponse.error("404", "找不到訂單：" + orderNo);
        }
        return ApiResponse.success(order);
    }

    @PutMapping("/{orderNo}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable String orderNo,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ApiResponse.error("400", "status 不可為空");
        }
        orderMapper.adminUpdateStatus(orderNo, newStatus);
        return ApiResponse.success("狀態已更新", null);
    }

    @DeleteMapping("/{orderNo}")
    public ApiResponse<Void> deleteOrder(@PathVariable String orderNo) {
        Order order = orderMapper.findByOrderNoSimple(orderNo);
        if (order == null) {
            return ApiResponse.error("404", "找不到訂單：" + orderNo);
        }
        orderMapper.adminDeleteOrder(orderNo);
        return ApiResponse.success("訂單已刪除", null);
    }
}
