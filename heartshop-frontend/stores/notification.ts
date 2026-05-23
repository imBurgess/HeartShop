import { defineStore } from "pinia";
import { ref } from "vue";
import { notificationService, type NotificationItem } from "~/services/notification";

export const useNotificationStore = defineStore("notification", () => {
  const unreadCount = ref(0);
  const items = ref<NotificationItem[]>([]);
  let pollTimer: ReturnType<typeof setInterval> | null = null;

  const fetchUnreadCount = async () => {
    try {
      const data = await notificationService.getUnreadCount();
      unreadCount.value = data?.count ?? 0;
    } catch (e) {
      console.error("[notification] getUnreadCount 失敗:", e);
    }
  };

  const fetchNotifications = async (page = 1, pageSize = 20) => {
    try {
      const data = await notificationService.getNotifications(page, pageSize);
      items.value = data?.items ?? [];
      unreadCount.value = data?.unreadCount ?? 0;
    } catch (_) {}
  };

  const markRead = async (notificationId: number) => {
    await notificationService.markRead(notificationId);
    const found = items.value.find((n) => n.notificationId === notificationId);
    if (found) {
      found.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }
  };

  const markAllRead = async () => {
    await notificationService.markAllRead();
    items.value.forEach((n) => (n.isRead = true));
    unreadCount.value = 0;
  };

  const startPolling = (intervalMs = 30000) => {
    if (pollTimer) return;
    fetchUnreadCount();
    pollTimer = setInterval(fetchUnreadCount, intervalMs);
  };

  const stopPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
  };

  return {
    unreadCount,
    items,
    fetchUnreadCount,
    fetchNotifications,
    markRead,
    markAllRead,
    startPolling,
    stopPolling,
  };
});
