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

        <!-- 右側：內容區 -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <header class="member-header">
            <p>會員中心</p>
          </header>
          <n-card size="small" class="member-card">
            <p class="greeting">Hi {{ memberName }}，</p>
            <p class="notice">
              申請退貨請於
              <span class="highlight">七日鑑賞期內</span>
              ，來信客服申請。退貨相關資訊請參考：
              <NuxtLink to="/ShopInfo/shopinfo?sec=return">
                售後服務說明
              </NuxtLink>
            </p>
          </n-card>

          <!-- 可用紅利 -->
          <n-card size="small" class="member-card bonus-card">
            <div class="bonus-block">
              <div class="bonus-label">可用紅利金</div>
              <div class="bonus-amount">
                <span class="value">{{ bonusPoints }}</span>
                <span class="unit">元</span>
              </div>
            </div>
          </n-card>

          <!-- 最新訂單紀錄 -->
          <n-card class="member-card" size="small" :bordered="false">
            <template #header>
              <div class="order-header">
                <span class="order-title">LATEST 訂單紀錄</span>
                <NuxtLink to="/member/orders" class="order-more">
                  查看全部訂單
                </NuxtLink>
              </div>
            </template>

            <n-data-table
              v-if="latestOrders.length > 0"
              :columns="columns"
              :data="latestOrders"
              :bordered="false"
              size="small"
            />
            <p v-else style="margin:0;font-size:13px;color:#aaa;text-align:center;padding:24px 0">
              目前尚無訂單紀錄
            </p>
          </n-card>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { h, ref, computed, onMounted, resolveComponent, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import type { DataTableColumns, MenuOption } from "naive-ui";
import { orderService } from "@/services/order";

const router = useRouter();
const route = useRoute();

/* ===== 左側選單目前選到哪一個 ===== */
const activeKey = ref<string | null>(null);

const getMenuKeyByPath = (path: string) => {
  if (path.startsWith("/member/orders")) return "orders";
  if (
    path.startsWith("/member/favorite") ||
    path.startsWith("/member/wishlist")
  )
    return "favorite";
  if (path.startsWith("/member/qa")) return "qa";
  if (path.startsWith("/member/profile")) return "profile";
  return "dashboard";
};

activeKey.value = getMenuKeyByPath(route.path);

watch(
  () => route.path,
  (newPath) => {
    activeKey.value = getMenuKeyByPath(newPath);
  }
);

const menuOptions: MenuOption[] = [
  { key: "dashboard", label: "會員中心" },
  { key: "favorite", label: "我的收藏" },
  { key: "orders", label: "訂單紀錄" },
  { key: "qa", label: "商品問答紀錄" },
  { key: "profile", label: "修改會員資料與密碼" },
];

const handleMenuSelect = (key: string) => {
  activeKey.value = key;
  switch (key) {
    case "dashboard":
      router.push("/member");
      break;
    case "favorite":
      router.push("/member/wishlist");
      break;
    case "orders":
      router.push("/member/orders");
      break;
    case "qa":
      router.push("/member/qa");
      break;
    case "profile":
      router.push("/member/profile");
      break;
  }
};

/* ===== 會員資訊（從 cookie 讀取） ===== */

// 讀取 memberInfo cookie
const memberInfoCookie = useCookie<{
  memberId: number;
  email: string;
  name: string;
} | null>("memberInfo");

// 顯示會員名稱（從 cookie 讀取，如果沒有則顯示預設值）
const memberName = computed(() => memberInfoCookie.value?.name || "訪客");

// 紅利點數（之後可接後端 API）
const bonusPoints = ref(0); // TODO: 之後改成呼叫後端 API 取得實際紅利

// ✅ 在客戶端檢查登入狀態（避免 SSR 問題）
onMounted(async () => {
  if (!memberInfoCookie.value) {
    navigateTo("/");
    return;
  }
  try {
    const all = await orderService.getMemberOrders();
    latestOrders.value = all.slice(0, 3).map((o) => ({
      orderNo: o.orderNo,
      date: new Date(o.createdAt).toLocaleDateString("zh-TW"),
      total: o.totalAmount,
      status: statusLabel(o.status),
    }));
  } catch (_) {
    // 載入失敗靜默處理，不影響其他卡片
  }
});

type OrderRow = {
  orderNo: string;
  date: string;
  total: number;
  status: string;
};

const columns: DataTableColumns<OrderRow> = [
  { title: "訂單編號", key: "orderNo" },
  { title: "訂購日期", key: "date" },
  { title: "訂單金額", key: "total" },
  { title: "狀態", key: "status" },
];

const latestOrders = ref<OrderRow[]>([]);

const statusLabel = (s: string) =>
  ({ pending: "待付款", PAID: "已付款", FAILED: "付款失敗", SHIPPED: "已出貨", COMPLETED: "已完成" }[s] ?? s);
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

.member-header p {
  text-align: center;
  margin-bottom: 24px;
  font-size: 25px;
  font-weight: bold;
}

.greeting {
  margin: 0 0 8px;
  font-weight: 600;
}

.notice {
  margin: 0;
  line-height: 1.6;
  color: #353535;
  font-size: 14px;
}

.notice .highlight {
  color: #353535;
  font-weight: 600;
}

/* 紅利區塊 */
.bonus-card {
  background: #faf6f2;
}

.bonus-block {
  display: flex;
  align-items: center;
}

.bonus-label {
  margin-right: 24px;
}

.bonus-amount .value {
  font-size: 24px;
  font-weight: 700;
  margin-right: 4px;
}

/* 訂單表格 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.order-title {
  font-weight: 600;
}

.order-more {
  font-size: 12px;
  text-decoration: underline;
  color: #353535;
}

/* RWD */
@media (max-width: 768px) {
  .member-page {
    padding: 16px 0;
  }

  .member-layout {
    margin: 0 8px;
  }
}
</style>
