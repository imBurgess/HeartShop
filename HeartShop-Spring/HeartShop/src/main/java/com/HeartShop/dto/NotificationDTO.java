package com.HeartShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private String type;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private OffsetDateTime createdAt;
}
