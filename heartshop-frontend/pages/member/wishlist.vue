<!-- pages/member/wishlist.vue -->
<template>
  <main class="member-page">
    <n-layout class="member-layout">
      <n-layout has-sider>
        <!-- 左側：會員選單（沿用你貼的版本） -->
        <n-layout-sider width="220" bordered class="member-sider">
          <n-menu
            v-model:value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
        </n-layout-sider>

        <!-- 右側：Wishlist 內容區 -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <!-- 頁面標題 -->
          <header class="wishlist-header">
            <p class="wishlist-zh">我的收藏</p>
          </header>

          <!-- Wishlist 表格區 -->
          <section class="wishlist">
            <div class="wishlist-table">
              <!-- 表頭 -->
              <div class="wishlist-header-row">
                <span class="col-product">商品名稱</span>
                <span class="col-price">單價</span>
                <span class="col-discount">折扣價</span>
                <span class="col-action">刪除</span>
              </div>

              <!-- 載入中 -->
              <div v-if="loading" class="empty-state">載入中...</div>

              <!-- 沒有商品 -->
              <div v-else-if="wishlistItems.length === 0" class="empty-state">
                目前沒有任何收藏商品
              </div>

              <!-- 商品列 -->
              <div
                v-else
                v-for="item in wishlistItems"
                :key="item.wishlistId"
                class="wishlist-row"
              >
                <!-- 商品名稱＋圖片 -->
                <div class="col-product">
                  <NuxtLink :to="`/product/${item.productId}`" class="product-block">
                    <n-image
                      :src="getFullImageUrl(item.productImage)"
                      :alt="item.productName"
                      width="140"
                      height="180"
                      object-fit="cover"
                      preview-disabled
                    />
                    <div class="product-info">
                      <p class="name-zh">{{ item.productName }}</p>
                      <p class="name-en">{{ item.productNameEn }}</p>
                    </div>
                  </NuxtLink>
                </div>

                <!-- 單價 -->
                <div class="col-price">
                  <span class="price">NT $ {{ (item.price || 0).toLocaleString() }}</span>
                </div>

                <!-- 折扣價 -->
                <div class="col-discount">
                  <span class="discount">
                    {{ item.discountPrice ? 'NT $ ' + item.discountPrice.toLocaleString() : '-' }}
                  </span>
                </div>

                <!-- 刪除 -->
                <div class="col-action">
                  <button
                    class="btn-delete"
                    type="button"
                    @click="removeItem(item.productId)"
                  >
                    🗑 取消收藏
                  </button>
                </div>
              </div>
            </div>

            <!-- 底部：總件數＋繼續購物 -->
            <footer class="wishlist-footer">
              <span class="count">
                【 共 {{ wishlistItems.length }} 件 】
              </span>
              <n-button
                color="#353535"
                class="continue-btn"
                @click="goShopping"
              >
                繼續購物 &gt;
              </n-button>
            </footer>
          </section>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { MenuOption } from "naive-ui";
import { useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { wishlistService, type WishlistItem } from "@/services/wishlist";

const router = useRouter();
const message = useMessage();

const activeKey = ref<string | null>("favorite");

const menuOptions: MenuOption[] = [
  { key: "dashboard", label: "會員中心" },
  { key: "favorite", label: "我的收藏" },
  { key: "orders", label: "訂單紀錄" },
  { key: "qa", label: "商品問答紀錄" },
  { key: "profile", label: "修改會員資料與密碼" },
];

const handleMenuSelect = (key: string) => {
  activeKey.value = key;
  const pathMap: Record<string, string> = {
    dashboard: "/member",
    favorite: "/member/wishlist",
    orders: "/member/orders",
    qa: "/member/qa",
    profile: "/member/profile",
  };
  router.push(pathMap[key] ?? "/member");
};

const loading = ref(true);
const wishlistItems = ref<WishlistItem[]>([]);

const getFullImageUrl = (url?: string) => {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${base}${url}`;
};

onMounted(async () => {
  try {
    wishlistItems.value = await wishlistService.getWishlist();
  } catch (err: any) {
    message.error(err.message || "載入收藏失敗");
  } finally {
    loading.value = false;
  }
});

const removeItem = async (productId: number) => {
  try {
    await wishlistService.remove(productId);
    wishlistItems.value = wishlistItems.value.filter((i) => i.productId !== productId);
    message.success("已取消收藏");
  } catch (err: any) {
    message.error(err.message || "操作失敗");
  }
};

const goShopping = () => router.push("/shop/popular");
</script>

<style scoped lang="scss">
/* 沿用你原本會員中心的基本框架 */
.member-page {
  padding: 40px 0 80px;
  display: flex;
  justify-content: center;
}

.member-layout {
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  background-color: #ffffff;
}

.member-sider {
  padding: 16px 12px;
  background-color: #fff;
}

/* Wishlist 標題 */
.wishlist-header {
  text-align: center;
  margin-bottom: 24px;

  .wishlist-zh {
    font-size: 25px;
    font-weight: bold;
  }
}

/* 表格框線 */
.wishlist-table {
  border-top: 1px solid #ccc;
  border-bottom: 1px solid #eee;
}

/* 表頭列 */
.wishlist-header-row {
  display: grid;
  grid-template-columns: minmax(0, 3fr) 1fr 1fr 110px;
  padding: 8px 16px;
  background-color: #f3f3f3;
  font-size: 13px;
}

/* 資料列 */
.wishlist-row {
  display: grid;
  grid-template-columns: minmax(0, 3fr) 1fr 1fr 110px;
  padding: 16px;
  column-gap: 24px;
  align-items: center;
  border-top: 1px solid #eee;
}

/* 商品區塊 */
.product-block {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  text-decoration: none;
  color: inherit;
}

.product-info {
  padding-top: 4px;

  .name-zh {
    font-size: 13px;
    line-height: 1.4;
    margin-bottom: 4px;
  }

  .name-en {
    font-size: 12px;
    color: #353535;
    line-height: 1.4;
  }
}

/* 價格欄位 */
.col-price,
.col-discount {
  font-size: 13px;
}

.price,
.discount {
  white-space: nowrap;
}

/* 刪除按鈕 */
.btn-delete {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  font-size: 12px;
  color: #c0392b;
  background: transparent;
  border: 1px solid #c0392b;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.18s, color 0.18s;

  &:hover {
    background: #c0392b;
    color: #fff;
  }
}

/* 空狀態 */
.empty-state {
  padding: 40px 16px;
  text-align: center;
  font-size: 14px;
}

/* 底部區塊 */
.wishlist-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px 0;
  font-size: 13px;

  .continue-btn {
    min-width: 180px;
  }
}

/* RWD */
@media (max-width: 768px) {
  .member-page {
    padding: 16px 0;
  }

  .member-layout {
    margin: 0 8px;
  }

  .wishlist-header-row {
    display: none;
  }

  .wishlist-row {
    grid-template-columns: 1fr;
    row-gap: 8px;
  }

  .col-price,
  .col-discount,
  .col-action {
    text-align: right;
  }
}
</style>
