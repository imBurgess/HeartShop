package com.HeartShop.service;

import com.HeartShop.dto.NotificationDTO;
import com.HeartShop.entity.Notification;
import com.HeartShop.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public void createQaReplyNotification(Long memberId, String productName, Long productId) {
        try {
            Notification n = new Notification();
            n.setMemberId(memberId);
            n.setType("QA_REPLY");
            n.setTitle("您的問題已獲得回覆");
            n.setContent("您對「" + productName + "」的提問已獲得管理員回覆，點此查看。");
            n.setLinkUrl("/member/qa");
            notificationMapper.insert(n);
            log.info("已建立商品問答通知，memberId={}, productId={}", memberId, productId);
        } catch (Exception e) {
            log.error("建立商品問答通知失敗，memberId={}: {}", memberId, e.getMessage());
        }
    }

    public void createOrderQaReplyNotification(Long memberId, String orderNo) {
        try {
            Notification n = new Notification();
            n.setMemberId(memberId);
            n.setType("QA_REPLY");
            n.setTitle("您的問題已獲得回覆");
            n.setContent("您對訂單「" + orderNo + "」的提問已獲得管理員回覆，點此查看。");
            n.setLinkUrl("/member/qa");
            notificationMapper.insert(n);
            log.info("已建立訂單問答通知，memberId={}, orderNo={}", memberId, orderNo);
        } catch (Exception e) {
            log.error("建立訂單問答通知失敗，memberId={}: {}", memberId, e.getMessage());
        }
    }

    public List<NotificationDTO> getNotifications(Long memberId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return notificationMapper.findByMemberId(memberId, offset, pageSize)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long memberId) {
        return notificationMapper.countUnread(memberId);
    }

    @Transactional
    public void markRead(Long notificationId, Long memberId) {
        notificationMapper.markRead(notificationId, memberId);
    }

    @Transactional
    public void markAllRead(Long memberId) {
        notificationMapper.markAllRead(memberId);
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(n.getNotificationId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setLinkUrl(n.getLinkUrl());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
