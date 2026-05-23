package com.HeartShop.mapper;

import com.HeartShop.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> findByMemberId(@Param("memberId") Long memberId,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    long countUnread(@Param("memberId") Long memberId);

    int markRead(@Param("notificationId") Long notificationId,
                 @Param("memberId") Long memberId);

    int markAllRead(@Param("memberId") Long memberId);
}
