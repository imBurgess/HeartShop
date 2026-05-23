<template>
  <header class="header">
    <div class="header-left">
      <div class="logo">
        <svg
          class="logo-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
            fill="currentColor"
          />
        </svg>
        <span class="logo-text">拾心市集</span>
        <span class="logo-badge">後台</span>
      </div>
    </div>

    <div class="header-right">
      <!-- 搜尋框 -->
      <div class="search-wrapper">
        <svg
          class="search-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        <input type="text" placeholder="搜尋功能..." class="search-input" />
      </div>

      <!-- 通知 -->
      <button class="icon-button" title="通知">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span class="notification-dot"></span>
      </button>

      <!-- 使用者資訊 -->
      <div class="user-info">
        <div class="user-avatar">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="user-details">
          <span class="user-name">{{ auth.user?.name ?? "管理員" }}</span>
          <span class="user-role">系統管理員</span>
        </div>
      </div>

      <!-- 登出按鈕 -->
      <button class="logout-button" @click="handleLogout" title="登出">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const auth = useAuthStore();

function handleLogout() {
  auth.logout();
  router.replace("/login");
}
</script>

<style scoped lang="scss">
.header {
  height: 64px;
  padding: 0 var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  /* 前台一致的大地色毛玻璃效果 */
  background-color: rgba(138, 137, 124, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 2px 12px rgba(90, 89, 80, 0.18);
  position: sticky;
  top: 0;
  z-index: 100;
  animation: slideInUp var(--transition-base);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: var(--font-weight-bold);
  font-size: 1.25rem;
  color: #ffffff;
}

.logo-icon {
  width: 28px;
  height: 28px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.logo-text {
  color: #ffffff;
  font-weight: var(--font-weight-bold);
  /* 覆蓋漸層透明文字，改為純白 */
  -webkit-text-fill-color: #ffffff;
}

.logo-badge {
  padding: 2px 8px;
  font-size: 0.75rem;
  font-weight: var(--font-weight-medium);
  color: rgba(138, 137, 124, 0.9);
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-full);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing);
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 18px;
  height: 18px;
  color: rgba(255, 255, 255, 0.7);
  pointer-events: none;
}

.search-input {
  width: 240px;
  height: 36px;
  padding: 0 12px 0 38px;
  font-size: 0.875rem;
  color: #353535;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-full);
  outline: none;
  transition: all var(--transition-base);

  &::placeholder {
    color: rgba(90, 89, 80, 0.6);
  }

  &:focus {
    width: 280px;
    background: #ffffff;
    border-color: rgba(255, 255, 255, 0.8);
    box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.25);
  }
}

.icon-button {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  background: transparent;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-base);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.15);
  }
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #e8c47a;
  border: 2px solid rgba(138, 137, 124, 0.9);
  border-radius: var(--radius-full);
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 6px 12px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-base);

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: #ffffff;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow);

  svg {
    width: 20px;
    height: 20px;
  }
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 0.875rem;
  font-weight: var(--font-weight-semibold);
  color: #ffffff;
}

.user-role {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.75);
}

.logout-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-base);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.15);
    border-color: rgba(255, 255, 255, 0.6);
    transform: translateY(-1px);
  }
}
</style>
