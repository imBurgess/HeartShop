<template>
  <main class="member-page">
    <n-layout class="member-layout">
      <n-layout has-sider>
        <!-- 左側：會員選單 -->
        <n-layout-sider width="220" bordered class="member-sider">
          <n-menu
            v-model:value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
        </n-layout-sider>

        <!-- 右側：Q&A 內容 -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <header class="qa-header">
            <p class="qa-zh">商品問答紀錄</p>
          </header>

          <n-tabs type="line" v-model:value="activeTab">
            <!-- 商品問答 -->
            <n-tab-pane name="product" tab="商品問答">
              <div class="qa-table-header"><span>提問紀錄</span></div>
              <div class="qa-table-body">
                <div v-if="productLoading" class="qa-empty">資料載入中…</div>
                <div v-else-if="productList.length === 0" class="qa-empty">尚未提問</div>
                <div v-else>
                  <div v-for="item in productList" :key="item.qaId" class="qa-row">
                    <div class="qa-question">
                      <p class="q-text">{{ item.question }}</p>
                      <p class="q-meta">
                        商品：{{ item.productName || '未知商品' }} ｜ {{ formatDate(item.createdAt) }}
                      </p>
                    </div>
                    <div v-if="item.answer" class="qa-answer">
                      <span class="a-label">回覆：</span>
                      <p class="a-text">{{ item.answer }}</p>
                    </div>
                    <div v-else class="qa-answer pending">尚未回覆</div>
                  </div>
                </div>
              </div>
            </n-tab-pane>

            <!-- 訂單問答 -->
            <n-tab-pane name="order" tab="訂單問答">
              <div class="qa-table-header"><span>提問紀錄</span></div>
              <div class="qa-table-body">
                <div v-if="orderLoading" class="qa-empty">資料載入中…</div>
                <div v-else-if="orderList.length === 0" class="qa-empty">尚未提問</div>
                <div v-else>
                  <div v-for="item in orderList" :key="item.qaId" class="qa-row">
                    <div class="qa-question">
                      <p class="q-text">{{ item.question }}</p>
                      <p class="q-meta">
                        訂單：{{ item.orderNo }} ｜ {{ formatDate(item.createdAt) }}
                      </p>
                    </div>
                    <div v-if="item.answer" class="qa-answer">
                      <span class="a-label">回覆：</span>
                      <p class="a-text">{{ item.answer }}</p>
                    </div>
                    <div v-else class="qa-answer pending">尚未回覆</div>
                  </div>
                </div>
              </div>
            </n-tab-pane>
          </n-tabs>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import type { MenuOption } from "naive-ui";
import { qaService, type QaItem } from "@/services/qa";

const router = useRouter();
const activeKey = ref<string | null>("qa");
const activeTab = ref("product");

const menuOptions: MenuOption[] = [
  { key: "dashboard", label: "會員中心" },
  { key: "favorite", label: "我的收藏" },
  { key: "orders", label: "訂單紀錄" },
  { key: "qa", label: "商品問答紀錄" },
  { key: "profile", label: "修改會員資料與密碼" },
];

const handleMenuSelect = (key: string) => {
  activeKey.value = key;
  const routes: Record<string, string> = {
    dashboard: "/member",
    favorite: "/member/wishlist",
    orders: "/member/orders",
    qa: "/member/qa",
    profile: "/member/profile",
  };
  router.push(routes[key] ?? "/member");
};

const productList = ref<QaItem[]>([]);
const orderList = ref<QaItem[]>([]);
const productLoading = ref(true);
const orderLoading = ref(true);

const formatDate = (dateStr: string) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

onMounted(async () => {
  const [pResult, oResult] = await Promise.allSettled([
    qaService.getMyQa(),
    qaService.getMyOrderQa(),
  ]);
  productList.value = pResult.status === "fulfilled" ? pResult.value : [];
  orderList.value = oResult.status === "fulfilled" ? oResult.value : [];
  productLoading.value = false;
  orderLoading.value = false;
});
</script>

<style scoped lang="scss">
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

.qa-header {
  text-align: center;
  margin-bottom: 24px;
}
.qa-zh {
  font-size: 25px;
  font-weight: bold;
  margin: 0;
}

.qa-table-header {
  background-color: #b3b3b3;
  color: #ffffff;
  padding: 8px 16px;
  font-size: 13px;
  text-align: center;
  margin-top: 12px;
}

.qa-table-body {
  border: 1px solid #e5e5e5;
  border-top: none;
  padding: 16px 24px 24px;
  min-height: 160px;
}

.qa-empty {
  font-size: 14px;
  color: #353535;
}

.qa-row {
  padding: 12px 0;
  border-bottom: 1px solid #eeeeee;

  &:last-child {
    border-bottom: none;
  }
}

.qa-question .q-text {
  font-size: 14px;
  margin: 0 0 4px;
}

.qa-question .q-meta {
  font-size: 12px;
  color: #888;
  margin: 0;
}

.qa-answer {
  margin-top: 6px;
  font-size: 13px;
  display: flex;
  gap: 4px;

  &.pending {
    color: #aaa;
    font-style: italic;
  }
}

.a-label {
  font-weight: 600;
  flex-shrink: 0;
}

.a-text {
  margin: 0;
}
</style>
