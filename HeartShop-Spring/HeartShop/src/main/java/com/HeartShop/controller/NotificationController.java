package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.NotificationDTO;
import com.HeartShop.service.NotificationService;
import com.HeartShop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<Map<String, Object>> getNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Long memberId = jwtUtil.getMemberIdFromToken(token.replace("Bearer ", ""));
        List<NotificationDTO> items = notificationService.getNotifications(memberId, page, pageSize);
        long unreadCount = notificationService.getUnreadCount(memberId);

        return ApiResponse.success(Map.of(
                "items", items,
                "unreadCount", unreadCount,
                "page", page,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String token) {

        Long memberId = jwtUtil.getMemberIdFromToken(token.replace("Bearer ", ""));
        long count = notificationService.getUnreadCount(memberId);
        return ApiResponse.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        Long memberId = jwtUtil.getMemberIdFromToken(token.replace("Bearer ", ""));
        notificationService.markRead(id, memberId);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(
            @RequestHeader("Authorization") String token) {

        Long memberId = jwtUtil.getMemberIdFromToken(token.replace("Bearer ", ""));
        notificationService.markAllRead(memberId);
        return ApiResponse.success(null);
    }
}
