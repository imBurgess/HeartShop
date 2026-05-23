<template>
  <aside class="aside">
    <nav class="menu">
      <div class="menu-section">
        <div class="menu-section-title">主選單</div>

        <router-link
          v-for="item in mainMenuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
        >
          <div class="menu-item-icon" v-html="item.icon"></div>
          <span class="menu-item-text">{{ item.label }}</span>
          <span v-if="item.badge" class="menu-item-badge">{{
            item.badge
          }}</span>
        </router-link>
      </div>

      <div class="menu-section">
        <div class="menu-section-title">商店管理</div>

        <router-link
          v-for="item in shopMenuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
        >
          <div class="menu-item-icon" v-html="item.icon"></div>
          <span class="menu-item-text">{{ item.label }}</span>
        </router-link>
      </div>

      <div class="menu-section">
        <div class="menu-section-title">系統設定</div>

        <router-link
          v-for="item in settingMenuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
        >
          <div class="menu-item-icon" v-html="item.icon"></div>
          <span class="menu-item-text">{{ item.label }}</span>
        </router-link>
      </div>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";

const route = useRoute();

const isActive = (path: string) => {
  if (path === "/") {
    return route.path === "/";
  }
  return route.path.startsWith(path);
};

// 主選單
const mainMenuItems = [
  {
    path: "/",
    label: "儀表板",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z" fill="currentColor"/></svg>`,
  },
  {
    path: "/products",
    label: "商品管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 7h-4V5l-2-2h-4L8 5v2H4c-1.1 0-2 .9-2 2v5c0 .75.4 1.38 1 1.73V19c0 1.11.89 2 2 2h14c1.11 0 2-.89 2-2v-3.28c.59-.35 1-.99 1-1.72V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zM4 9h16v5h-5v-3H9v3H4V9zm9 6h-2v-2h2v2zm6 4H5v-3h4v1h6v-1h4v3z" fill="currentColor"/></svg>`,
  },
  {
    path: "/orders",
    label: "訂單管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z" fill="currentColor"/><path d="M7 12h10M7 16h7M7 8h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>`,
  },
  {
    path: "/members",
    label: "會員管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" fill="currentColor"/></svg>`,
  },
];

// 商店管理選單
const shopMenuItems = [
  {
    path: "/categories",
    label: "商品分類頁面管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 2l-5.5 9h11L12 2zM5 22c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm11.5-7.5h4v4h-4v-4z" fill="currentColor"/></svg>`,
  },
  {
    path: "/inventory",
    label: "庫存管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zm-5 12H9v-2h6v2zm5-7H4V4h16v3z" fill="currentColor"/></svg>`,
  },
  {
    path: "/home-blocks",
    label: "首頁區塊",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zM11 12H9V9h2v3zm4 0h-2V9h2v3zm4 0h-2V9h2v3zm-8 4H9v-3h2v3zm4 0h-2v-3h2v3zm4 0h-2v-3h2v3z" fill="currentColor"/></svg>`,
  },
  {
    path: "/qa",
    label: "問答管理",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z" fill="currentColor"/></svg>`,
  },
];

// 系統設定選單
const settingMenuItems = [
  {
    path: "/settings",
    label: "系統設定",
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14.4 2.81c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z" fill="currentColor"/></svg>`,
  },
];
</script>

<style scoped lang="scss">
.aside {
  width: 260px;
  height: 100%;
  background: var(--color-white);
  border-right: 1px solid var(--color-gray-200);
  overflow-y: auto;
  animation: slideInLeft var(--transition-base);
}

.menu {
  padding: var(--spacing);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-section-title {
  padding: var(--spacing-sm) var(--spacing-sm);
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-600);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 12px var(--spacing-sm);
  font-size: 0.875rem;
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
  text-decoration: none;
  border-radius: var(--radius);
  transition: all var(--transition-base);
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    width: 3px;
    height: 100%;
    background: var(--color-primary);
    transform: scaleY(0);
    transition: transform var(--transition-base);
  }

  &:hover {
    color: var(--color-primary-dark);
    background: var(--color-primary-bg);
    transform: translateX(2px);
  }

  &.active {
    color: var(--color-primary-dark);
    background: var(--color-primary-bg);
    font-weight: var(--font-weight-semibold);
    border: 1px solid rgba(138, 137, 124, 0.2);

    &::before {
      transform: scaleY(1);
    }

    .menu-item-icon {
      color: var(--color-primary);
    }
  }
}

.menu-item-icon {
  width: 20px;
  height: 20px;
  color: var(--color-gray-500);
  transition: color var(--transition-base);

  svg {
    width: 100%;
    height: 100%;
  }
}

.menu-item-text {
  flex: 1;
}

.menu-item-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-white);
  background: var(--color-primary);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
}

/* 滾動條樣式 */
.aside::-webkit-scrollbar {
  width: 4px;
}

.aside::-webkit-scrollbar-track {
  background: transparent;
}

.aside::-webkit-scrollbar-thumb {
  background: var(--color-gray-300);
  border-radius: var(--radius-full);

  &:hover {
    background: var(--color-gray-400);
  }
}
</style>
