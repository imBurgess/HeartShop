<template>
  <div class="site-layout">
    <header class="nav-container">
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
              @submit.prevent=""
            >
              <input
                type="search"
                placeholder="搜尋商品..."
                aria-label="搜尋"
              />
              <button type="submit">search</button>
            </form>

            <button
              class="icon-btn notification"
              aria-label="消息中心"
            ></button>

            <NuxtLink
              to="/cart"
              class="icon-btn cart"
              aria-label="購物車"
            ></NuxtLink>

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

              <li>
                <n-dropdown
                  trigger="hover"
                  placement="bottom-start"
                  :options="categoryOptions"
                  @select="handleCategorySelect"
                >
                  <button type="button" class="nav-link-btn">商品分類</button>
                </n-dropdown>
              </li>

              <li>
                <n-dropdown
                  trigger="hover"
                  placement="bottom-start"
                  :options="goodsUploadOptions"
                  @select="goodsUploadSelect"
                >
                  <button type="button" class="nav-link-btn">門市收購</button>
                </n-dropdown>
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
import { ref, computed, onMounted } from "vue";
import { categoryService, type Category } from "@/services/category";

// ✅ Nuxt3 cookie（用 token 判斷登入狀態）
const token = useCookie<string | null>("token");
const memberInfo = useCookie<{
  memberId: number;
  email: string;
  name: string;
} | null>("memberInfo");

const isLoggedIn = computed(() => !!token.value);

const router = useRouter();

// loginRegister：控制登入視窗開關
const showLogin = ref(false);

const handleLoginSuccess = () => {
  // ✅ 登入成功後關掉視窗
  showLogin.value = false;
  // 此時若 LoginRegister 有把 token 存到 cookie，memberOptions 會自動變成「登出」
};

// navbar 下拉選單 - 動態載入分類
const categories = ref<Category[]>([]);
const categoryOptions = computed<DropdownOption[]>(() => [
  { label: "人氣商品推薦", key: "/shop/popular" },
  ...categories.value.map((cat) => ({
    label: `| ${cat.nameZh}`,
    key: `/shop/${cat.slug}`,
  })),
]);

const handleCategorySelect = (key: string | number) => {
  router.push(key as string);
};

const goodsUploadOptions: DropdownOption[] = [
  { label: "★ 填寫表單", key: "/GoodsUpload/goodsupload" },
];
const goodsUploadSelect = (key: string | number) => {
  router.push(key as string);
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

// ✅ 會員選單：未登入顯示「登入／註冊」，已登入顯示「登出」
const memberOptions = computed<DropdownOption[]>(() => {
  if (!isLoggedIn.value) {
    return [
      { label: "★ 訂單資訊", key: "/member/orders" },
      { label: "★ 收藏專區", key: "/member/wishlist" },
      { label: "★ 會員專區", key: "/member" },
      { label: "★ 登入／註冊", key: "login-modal" },
    ];
  }

  return [
    { label: "★ 訂單資訊", key: "/member/orders" },
    { label: "★ 收藏專區", key: "/member/wishlist" },
    { label: "★ 會員專區", key: "/member" },
    { label: "★ 登出", key: "logout" },
  ];
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

  if (!isLoggedIn.value && requiresAuth(k)) {
    showLogin.value = true;
    return;
  }

  await router.push(k);
};

// 載入分類資料
onMounted(async () => {
  try {
    console.log("開始載入分類...");
    const response = await categoryService.getActiveCategories();
    console.log("分類 API 回傳:", response);

    if (response && Array.isArray(response)) {
      console.log("分類資料:", response);
      // 過濾啟用中的分類並排序
      const activeCategories = response.filter((cat: any) => cat.isActive);
      categories.value = activeCategories.sort(
        (a: any, b: any) => a.sortOrder - b.sortOrder
      );
      console.log("處理後的分類:", categories.value);
      console.log("categoryOptions:", categoryOptions.value);
    }
  } catch (error) {
    console.error("載入分類失敗:", error);
  }
});
</script>

<style lang="scss" src="@/assets/scss/layout/layout.scss"></style>
