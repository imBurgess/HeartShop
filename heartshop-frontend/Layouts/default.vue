<template>
  <div class="site-layout">
    <header :class="['nav-container', { 'is-scrolled': isScrolled }]">
      <div class="container">
        <div class="nav-top">
          <!-- 左：LOGO -->
          <a href="/" class="brand" aria-label="拾心市集首頁">拾心市集</a>

          <!-- 右：搜尋 + 使用者區塊 -->
          <div class="actions">
            <form
              class="search"
              role="search"
              aria-label="站內搜尋"
              @submit.prevent="handleSearch"
            >
              <input
                v-model.trim="searchQuery"
                type="search"
                placeholder="搜尋商品..."
                aria-label="搜尋"
              />
              <button type="submit">search</button>
            </form>

            <n-popover
              trigger="hover"
              placement="bottom-end"
              :show-arrow="false"
              :raw="true"
              @update:show="onNotifPopoverShow"
            >
              <template #trigger>
                <div class="icon-btn notification" style="position: relative; cursor: pointer;" aria-label="消息中心">
                  <n-badge
                    v-if="notificationStore.unreadCount > 0"
                    :value="notificationStore.unreadCount"
                    :max="99"
                    style="position: absolute; top: -5px; right: -5px;"
                  />
                </div>
              </template>

              <div class="notif-dropdown">
                <div class="notif-header">
                  <span class="notif-header-title">消息通知</span>
                  <button
                    v-if="notificationStore.unreadCount > 0"
                    class="notif-read-all"
                    @click="notificationStore.markAllRead()"
                  >全部已讀</button>
                </div>

                <div v-if="!isLoggedIn" class="notif-empty">請先登入查看消息</div>
                <template v-else>
                  <div v-if="notificationStore.items.length === 0" class="notif-empty">目前沒有消息</div>
                  <div v-else class="notif-list">
                    <div
                      v-for="item in notificationStore.items.slice(0, 5)"
                      :key="item.notificationId"
                      class="notif-item"
                      :class="{ 'notif-unread': !item.isRead }"
                      @click="onNotifClick(item)"
                    >
                      <div class="notif-item-title">{{ item.title }}</div>
                      <div class="notif-item-content">{{ item.content }}</div>
                      <div class="notif-item-time">{{ formatNotifTime(item.createdAt) }}</div>
                    </div>
                  </div>
                  <div class="notif-footer">
                    <span v-if="notifNoMsgTip" class="notif-no-msg-tip">目前沒有消息</span>
                    <a class="notif-all-link" @click.prevent="handleViewAll">查看全部 →</a>
                  </div>
                </template>
              </div>
            </n-popover>

            <NuxtLink
              to="/cart"
              class="icon-btn cart"
              aria-label="購物車"
              style="position: relative;"
            >
              <n-badge v-if="cartStore.totalQty > 0" :value="cartStore.totalQty" :max="99" style="position: absolute; top: -5px; right: -5px;" />
            </NuxtLink>

            <n-dropdown
              trigger="hover"
              placement="bottom-end"
              :options="memberOptions"
              @select="handleMemberSelect"
            >
              <button class="icon-btn user" aria-label="會員中心"></button>
            </n-dropdown>
          </div>

          <!-- 中：導覽列 -->
          <nav class="primary-nav" aria-label="主導覽列">
            <ul>
              <li><NuxtLink to="/" class="home">首頁</NuxtLink></li>

              <li class="navCategoryMenu">
                <button type="button" class="nav-link-btn">商品分類</button>
                <div class="navCategoryDropdown">
                  <ul class="navCatList">
                    <li>
                      <button class="navCatItem" @click="router.push('/shop/popular')">人氣商品推薦</button>
                    </li>
                    <li v-for="parent in navMenuTree" :key="parent.slug">
                      <div class="navCatParentRow">
                        <button
                          class="navCatItem"
                          @click="router.push(`/shop/${parent.slug}`)"
                        >★ {{ parent.nameZh }}</button>
                        <button
                          v-if="parent.children.length > 0"
                          class="navCatToggle"
                          @click.stop="toggleNavParent(parent.slug)"
                        >{{ navExpandedParents[parent.slug] ? '▲' : '▼' }}</button>
                      </div>
                      <ul v-if="parent.children.length > 0 && navExpandedParents[parent.slug]" class="navCatSubList">
                        <li v-for="sub in parent.children" :key="sub.slug">
                          <button
                            class="navCatItem navCatSubItem"
                            @click="router.push(`/shop/${sub.slug}`)"
                          >★ {{ sub.nameZh }}</button>
                        </li>
                      </ul>
                    </li>
                    <li class="navCatDivider"></li>
                    <li>
                      <button class="navCatItem" @click="router.push('/shop/popular')">更多...</button>
                    </li>
                  </ul>
                </div>
              </li>

              <li>
                <n-dropdown
                  trigger="hover"
                  placement="bottom-start"
                  :options="buyContentOptions"
                  @select="buyContentSelect"
                >
                  <button type="button" class="nav-link-btn">購物說明</button>
                </n-dropdown>
              </li>

              <li>
                <n-dropdown
                  trigger="hover"
                  placement="bottom-start"
                  :options="aboutUsOptions"
                  @select="aboutUsSelect"
                >
                  <button type="button" class="nav-link-btn">關於我們</button>
                </n-dropdown>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </header>

    <!-- 會員登入 / 註冊彈窗 -->
    <LoginRegister
      v-model:show="showLogin"
      @login-success="handleLoginSuccess"
    />

    <main>
      <slot />
    </main>

    <footer class="site-footer" role="contentinfo">
      <div class="site-footer_inner">
        <div class="site-footer_line" aria-hidden="true"></div>

        <div class="site-footer_grid">
          <section class="footer-col">
            <h3 class="footer-col_title">關於我們</h3>
            <ul class="footer-col_links">
              <li><NuxtLink to="/about">商店簡介</NuxtLink></li>
              <li><NuxtLink to="/about">徵才資訊</NuxtLink></li>
            </ul>
          </section>

          <section class="footer-col">
            <h3 class="footer-col_title">客服資訊</h3>
            <ul class="footer-col_links">
              <li><NuxtLink to="/about">客服留言</NuxtLink></li>
              <li><NuxtLink to="/about">聯絡我們</NuxtLink></li>
            </ul>
          </section>

          <section class="footer-col">
            <h3 class="footer-col_title">隱私權及網站使用條款</h3>
            <ul class="footer-col_links">
              <li class="footer-links_members">
                <NuxtLink to="/ShopInfo/shopinfo">會員權益說明</NuxtLink>
              </li>
            </ul>

            <nav class="footer-social" aria-label="社群連結">
              <a
                href="#"
                target="_blank"
                rel="noopener"
                aria-label="Facebook"
                class="footer-social_icon footer-social_icon--fb"
              ></a>
              <a
                href="#"
                target="_blank"
                rel="noopener"
                aria-label="Instagram"
                class="footer-social_icon footer-social_icon--ig"
              ></a>
              <a
                href="#"
                target="_blank"
                rel="noopener"
                aria-label="threads"
                class="footer-social_icon footer-social_icon--threads"
              ></a>
            </nav>
          </section>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import type { DropdownOption } from "naive-ui";
import { useRouter } from "vue-router";
import { ref, computed, onMounted, onUnmounted } from "vue";
import { categoryService, type Category } from "@/services/category";
import { useCartStore } from "@/stores/cart";
import { useNotificationStore } from "@/stores/notification";
import type { NotificationItem } from "@/services/notification";

const config = useRuntimeConfig();

// ✅ Nuxt3 cookie（用 token 判斷登入狀態）
const token = useCookie<string | null>("token");
const memberInfo = useCookie<{
  memberId: number;
  email: string;
  name: string;
  role?: string;
} | null>("memberInfo");

const isLoggedIn = computed(() => !!token.value);
const isAdmin = computed(() => memberInfo.value?.role === "ADMIN");

const router = useRouter();
const cartStore = useCartStore();
const notificationStore = useNotificationStore();

// ── 通知 ──
const notifNoMsgTip = ref(false);
let notifTipTimer: ReturnType<typeof setTimeout> | null = null;

const onNotifPopoverShow = (show: boolean) => {
  if (show && isLoggedIn.value) {
    notificationStore.fetchNotifications();
  }
};

const handleViewAll = () => {
  if (notificationStore.items.length === 0) {
    notifNoMsgTip.value = true;
    if (notifTipTimer) clearTimeout(notifTipTimer);
    notifTipTimer = setTimeout(() => { notifNoMsgTip.value = false; }, 2000);
    return;
  }
  router.push("/member/qa");
};

const onNotifClick = async (item: NotificationItem) => {
  if (!item.isRead) {
    await notificationStore.markRead(item.notificationId);
  }
  router.push(item.linkUrl || "/member/qa");
};

const formatNotifTime = (dateStr: string) => {
  if (!dateStr) return "";
  const diff = Date.now() - new Date(dateStr).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return "剛剛";
  if (m < 60) return `${m} 分鐘前`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h} 小時前`;
  return `${Math.floor(h / 24)} 天前`;
};

// ── 搜尋 ──
const searchQuery = ref("");
const handleSearch = () => {
  const q = searchQuery.value.trim();
  if (!q) return;
  router.push(`/search?q=${encodeURIComponent(q)}`);
  searchQuery.value = "";
};

// 導覽列滾動狀態
const isScrolled = ref(false);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10;
};

// loginRegister：控制登入視窗開關
const showLogin = ref(false);

const handleLoginSuccess = () => {
  // ✅ 登入成功後關掉視窗
  showLogin.value = false;
  // 此時若 LoginRegister 有把 token 存到 cookie，memberOptions 會自動變成「登出」
};

// navbar 商品分類自訂下拉
const categories = ref<Category[]>([]);
const navExpandedParents = ref<Record<string, boolean>>({});

const navMenuTree = computed(() => {
  const parents = categories.value.filter((c) => !c.parentId);
  const subs = categories.value.filter((c) => c.parentId);
  return parents.map((parent) => ({
    ...parent,
    children: subs.filter((c) => c.parentId === parent.categoryId),
  }));
});

const toggleNavParent = (slug: string) => {
  navExpandedParents.value[slug] = !navExpandedParents.value[slug];
};

const buyContentOptions: DropdownOption[] = [
  { label: "★ 購物須知", key: "/ShopInfo/shopinfo?sec=notice" },
  { label: "★ 付款方式", key: "/ShopInfo/shopinfo?sec=pay" },
  { label: "★ 運送方式", key: "/ShopInfo/shopinfo?sec=ship" },
  { label: "★ 退換貨方式", key: "/ShopInfo/shopinfo?sec=return" },
];
const buyContentSelect = (key: string | number) => {
  router.push(key as string);
};

const aboutUsOptions: DropdownOption[] = [
  { label: "★品牌故事", key: "/about" },
];
const aboutUsSelect = (key: string | number) => {
  router.push(key as string);
};

// ✅ 會員選單：未登入顯示「登入／註冊」，已登入顯示「登出」；管理員額外顯示「管理後台」
const memberOptions = computed<DropdownOption[]>(() => {
  if (!isLoggedIn.value) {
    return [
      { label: "★ 會員專區", key: "/member" },
      { label: "★ 訂單資訊", key: "/member/orders" },
      { label: "★ 收藏專區", key: "/member/wishlist" },
      { label: "★ 登入／註冊", key: "login-modal" },
    ];
  }

  const options: DropdownOption[] = [
    { label: "★ 會員專區", key: "/member" },
    { label: "★ 訂單資訊", key: "/member/orders" },
    { label: "★ 收藏專區", key: "/member/wishlist" },
  ];

  if (isAdmin.value) {
    options.push({ label: "⚙ 管理後台", key: "admin-panel" });
  }

  options.push({ label: "★ 登出", key: "logout" });
  return options;
});

const requiresAuth = (path: string) => path.startsWith("/member");

const logout = async () => {
  // ✅ 清除 token 和會員資訊
  token.value = null;
  memberInfo.value = null;
  await router.push("/");
};

// ✅ 未登入點「會員專區」(或任何 /member...) → 彈登入視窗，不跳轉
const handleMemberSelect = async (key: string | number) => {
  const k = String(key);

  if (k === "login-modal") {
    showLogin.value = true;
    return;
  }

  if (k === "logout") {
    await logout();
    return;
  }

  if (k === "admin-panel") {
    window.open(config.public.adminUrl as string, "_blank");
    return;
  }

  if (!isLoggedIn.value && requiresAuth(k)) {
    showLogin.value = true;
    return;
  }

  await router.push(k);
};

// 同步最新 role（解決後台升級權限後前端未更新的問題）
const syncMemberRole = async () => {
  if (!token.value) return;
  try {
    const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const res = await fetch(`${apiBase}/api/members/me`, {
      headers: { Authorization: `Bearer ${token.value}` },
    });
    if (res.ok) {
      const json = await res.json();
      if (json.code === "0000" && json.data && memberInfo.value) {
        memberInfo.value = { ...memberInfo.value, role: json.data.role };
      }
    }
  } catch (_) {}
};

// 載入分類資料
onMounted(async () => {
  window.addEventListener("scroll", handleScroll);
  handleScroll();

  if (isLoggedIn.value) {
    cartStore.fetchCart();
    syncMemberRole();
    notificationStore.startPolling();
  }

  try {
    const response = await categoryService.getActiveCategories();
    if (response && Array.isArray(response)) {
      const activeCategories = response.filter((cat: any) => cat.isActive);
      categories.value = activeCategories.sort(
        (a: any, b: any) => a.sortOrder - b.sortOrder,
      );
    }
  } catch (error) {
    console.error("載入分類失敗:", error);
  }
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
  notificationStore.stopPolling();
});
</script>

<style lang="scss" src="@/assets/scss/layout/layout.scss"></style>
