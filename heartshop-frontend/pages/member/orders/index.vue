<template>
  <main class="member-page">
    <n-layout class="member-layout">
      <n-layout has-sider>
        <n-layout-sider width="220" bordered class="member-sider">
          <n-menu
            v-model:value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
        </n-layout-sider>

        <n-layout-content content-style="padding: 24px 32px 40px;">
          <header class="order-header">
            <p class="order-zh">訂單查詢</p>
          </header>

          <section class="order-intro">
            <p>Hi {{ memberName }}，</p>
            <p>
              申請退貨請於
              <span class="highlight">七日鑑賞期內（七日含假日）</span>
              ，來信客服申請。退貨相關資訊請參考：
              <a
                href="https://senseofplace.tokyo/zh-TW/AfterSales"
                target="_blank"
                rel="noopener noreferrer"
              >
                售後服務說明
              </a>
            </p>
            <p class="note">＊僅保留90天的訂單資料供提供查詢</p>
          </section>

          <section class="order-list">
            <div class="order-list-header">
              <span class="header-label">LATEST 訂單紀錄</span>
            </div>

            <div class="order-table-header">
              <span class="col-no">訂單編號</span>
              <span class="col-date">訂單日期</span>
              <span class="col-status">訂單狀態</span>
              <span class="col-amount">金額</span>
              <span class="col-action">明細</span>
            </div>

            <div v-if="loading" class="empty-state">資料載入中…</div>
            <div v-else-if="error" class="empty-state">{{ error }}</div>
            <div v-else-if="orders.length === 0" class="empty-state">
              目前沒有任何訂單紀錄
            </div>

            <div
              v-else
              v-for="o in pagedOrders"
              :key="o.orderNo"
              class="order-row"
            >
              <div class="col-no">
                <NuxtLink :to="`/member/orders/${o.orderNo}`" class="order-no">
                  {{ o.orderNo }}
                </NuxtLink>
              </div>

              <div class="col-date">{{ o.date }}</div>
              <div class="col-status">{{ o.status }}</div>
              <div class="col-amount">NT{{ formatMoney(o.amount) }}</div>

              <div class="col-action">
                <NuxtLink
                  :to="`/member/orders/${o.orderNo}`"
                  class="detail-link"
                >
                  訂單明細
                </NuxtLink>
              </div>
            </div>
          </section>

          <footer
            v-if="!loading && !error && pageCount > 1"
            class="order-pagination"
          >
            <button
              class="page-btn"
              type="button"
              :disabled="page === 1"
              @click="goPage(page - 1)"
            >
              ＜
            </button>

            <button
              v-for="p in pageCount"
              :key="p"
              class="page-btn"
              type="button"
              :class="{ active: p === page }"
              @click="goPage(p)"
            >
              {{ p }}
            </button>

            <button
              class="page-btn"
              type="button"
              :disabled="page === pageCount"
              @click="goPage(page + 1)"
            >
              ＞
            </button>
          </footer>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from "vue";
import type { MenuOption } from "naive-ui";
import { useRouter } from "vue-router";
import { orderService, type Order } from "~/services/order";

const router = useRouter();

// 讀取 memberInfo cookie
const memberInfoCookie = useCookie<{
  memberId: number;
  email: string;
  name: string;
} | null>("memberInfo");

// 顯示會員名稱（從 cookie 讀取，如果沒有則顯示預設值）
const memberName = computed(() => memberInfoCookie.value?.name || "訪客");

// menu
const activeKey = ref<string | null>("orders");
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

// data states
const loading = ref(true);
const error = ref<string | null>(null);
const orders = ref<Order[]>([]);

const formatMoney = (n: number) => new Intl.NumberFormat("zh-TW").format(n);

const fetchOrders = async () => {
  loading.value = true;
  error.value = null;
  try {
    const res = await orderService.list();
    orders.value = Array.isArray(res) ? res : (res as any)?.data ?? [];
  } catch (e: any) {
    error.value = e?.message ?? "訂單載入失敗";
    orders.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(fetchOrders);

// pagination
const page = ref(1);
const pageSize = 10;

const pageCount = computed(() =>
  Math.max(1, Math.ceil(orders.value.length / pageSize))
);

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize;
  return orders.value.slice(start, start + pageSize);
});

watch(pageCount, (pc) => {
  if (page.value > pc) page.value = pc;
});

const goPage = (p: number) => {
  if (p < 1 || p > pageCount.value) return;
  page.value = p;
};
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
} /* 標題區 */
.order-header {
  text-align: center;
  margin-bottom: 24px;
  .order-zh {
    font-size: 25px;
    font-weight: bold;
  }
} /* 說明文字 */
.order-intro {
  margin-bottom: 16px;
  font-size: 13px;
  line-height: 1.6;
  .highlight {
    font-weight: 700;
  }
  a {
    color: #c00;
    text-decoration: underline;
  }
  .note {
    margin-top: 4px;
    color: #666;
  }
} /* 訂單列表整體 */
.order-list {
  border-top: 1px solid #ccc;
  border-bottom: 1px solid #eee;
  font-size: 13px;
} /* 灰底 LATEST 列 */
.order-list-header {
  background-color: #b3b3b3;
  color: #fff;
  padding: 6px 16px;
  .header-label {
    font-size: 13px;
  }
} /* 欄位標題列 */
.order-table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 100px;
  padding: 8px 16px;
  background-color: #f3f3f3;
  border-bottom: 1px solid #ddd;
  span {
    display: inline-block;
  }
} /* 每一列資料 */
.order-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 100px;
  padding: 10px 16px;
  border-top: 1px solid #eee;
  align-items: center;
} /* 欄位對齊 */
.col-no {
  .order-no {
    color: #d00000;
    text-decoration: none;
  }
}
.col-date,
.col-status {
  color: #333;
}
.col-amount {
  text-align: right;
}
.col-action {
  text-align: center;
  .detail-link {
    color: #333;
    text-decoration: underline;
    font-size: 12px;
  }
} /* 空狀態 */
.empty-state {
  padding: 40px 16px;
  text-align: center;
} /* 分頁 */
.order-pagination {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 16px;
  .page-btn {
    min-width: 32px;
    height: 28px;
    border: 1px solid #ccc;
    background: #fff;
    font-size: 12px;
    cursor: pointer;
    &.active {
      background: #333;
      color: #fff;
    }
    &:disabled {
      cursor: default;
      color: #aaa;
      border-color: #ddd;
      background: #f7f7f7;
    }
  }
} /* RWD */
@media (max-width: 768px) {
  .member-page {
    padding: 16px 0;
  }
  .member-layout {
    margin: 0 8px;
  }
  .order-table-header {
    display: none;
  }
  .order-row {
    grid-template-columns: 1fr 1fr;
    grid-template-areas: "no no" "date status" "amount action";
    row-gap: 4px;
    .col-no {
      grid-area: no;
    }
    .col-date {
      grid-area: date;
    }
    .col-status {
      grid-area: status;
    }
    .col-amount {
      grid-area: amount;
      text-align: left;
    }
    .col-action {
      grid-area: action;
      text-align: right;
    }
  }
}
</style>
