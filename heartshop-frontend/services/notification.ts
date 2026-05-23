import { apiFetch } from "./http/client";

export interface NotificationItem {
  notificationId: number;
  type: string;
  title: string;
  content: string;
  linkUrl: string;
  isRead: boolean;
  createdAt: string;
}

export const notificationService = {
  async getNotifications(page = 1, pageSize = 20) {
    return apiFetch<{ items: NotificationItem[]; unreadCount: number; page: number; pageSize: number }>(
      `/api/notifications?page=${page}&pageSize=${pageSize}`
    );
  },

  async getUnreadCount() {
    return apiFetch<{ count: number }>("/api/notifications/unread-count");
  },

  async markRead(notificationId: number) {
    return apiFetch<void>(`/api/notifications/${notificationId}/read`, { method: "PUT" });
  },

  async markAllRead() {
    return apiFetch<void>("/api/notifications/read-all", { method: "PUT" });
  },
};
