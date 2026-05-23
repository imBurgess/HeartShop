package com.HeartShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long memberId;
    private String type;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private OffsetDateTime createdAt;
}
