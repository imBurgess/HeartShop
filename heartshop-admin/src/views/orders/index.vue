<template>
  <div class="orders-page">
    <n-card title="訂單管理" :bordered="false">

      <!-- 篩選區 -->
      <n-space :size="12" style="margin-bottom: 16px" wrap>
        <n-input
          v-model:value="filters.keyword"
          placeholder="搜尋訂單編號或收件人姓名"
          clearable
          style="width: 280px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" style="width:16px;height:16px">
              <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </template>
        </n-input>

        <n-select
          v-model:value="filters.status"
          placeholder="訂單狀態"
          clearable
          style="width: 160px"
          :options="statusOptions"
        />

        <n-date-picker
          v-model:value="filters.dateRange"
          type="daterange"
          clearable
          style="width: 300px"
          start-placeholder="Start Date"
          end-placeholder="End Date"
        />

        <n-button type="primary" @click="handleSearch">搜尋</n-button>
        <n-button @click="handleReset">重置</n-button>
      </n-space>

      <!-- 統計小卡 -->
      <n-space :size="12" style="margin-bottom: 16px" wrap>
        <div v-for="s in statCards" :key="s.key" class="stat-chip"
          :style="{ borderColor: s.color }"
          :class="{ active: filters.status === s.key }"
          @click="quickFilter(s.key)"
        >
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-count" :style="{ color: s.color }">{{ s.count }}</span>
        </div>
      </n-space>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="orders"
        :loading="loading"
        :row-key="(row) => row.orderNo"
        :pagination="false"
      />

      <!-- 分頁 -->
      <div style="display:flex; justify-content:flex-end; margin-top:16px">
        <n-pagination
          v-model:page="currentPage"
          :page-count="totalPages"
          :page-size="pageSize"
          show-quick-jumper
          @update:page="loadOrders"
        />
      </div>
    </n-card>

    <!-- 訂單詳情 Drawer -->
    <n-drawer v-model:show="drawerOpen" :width="520" placement="right">
      <n-drawer-content :title="`訂單詳情 — ${drawerOrder?.orderNo ?? ''}`" closable>
        <div v-if="detailLoading" style="text-align:center;padding:40px">
          <n-spin size="large" />
        </div>
        <template v-else-if="drawerOrder">
          <!-- 狀態修改 -->
          <n-card size="small" style="margin-bottom:16px">
            <div style="display:flex;align-items:center;gap:12px">
              <span style="font-size:13px;color:#888;flex-shrink:0">訂單狀態</span>
              <n-select
                v-model:value="editStatus"
                :options="statusOptions"
                size="small"
                style="flex:1"
              />
              <n-button
                type="primary"
                size="small"
                :loading="statusUpdating"
                @click="handleUpdateStatus"
              >
                更新
              </n-button>
            </div>
          </n-card>

          <!-- 基本資訊 -->
          <n-descriptions :columns="1" label-placement="left" size="small" bordered>
            <n-descriptions-item label="訂單編號">
              <span style="font-family:monospace">{{ drawerOrder.orderNo }}</span>
            </n-descriptions-item>
            <n-descriptions-item label="建立時間">{{ formatDate(drawerOrder.createdAt) }}</n-descriptions-item>
            <n-descriptions-item label="付款方式">{{ paymentLabel(drawerOrder.paymentMethod) }}</n-descriptions-item>
            <n-descriptions-item label="物流方式">{{ shippingLabel(drawerOrder.shippingMethod) }}</n-descriptions-item>
            <n-descriptions-item label="收件人">{{ drawerOrder.receiverName }}</n-descriptions-item>
            <n-descriptions-item label="聯絡電話">{{ drawerOrder.receiverPhone }}</n-descriptions-item>
            <n-descriptions-item label="收件地址">{{ drawerOrder.receiverAddress }}</n-descriptions-item>
          </n-descriptions>

          <!-- 商品明細 -->
          <div style="margin-top:16px">
            <p style="font-size:13px;font-weight:600;margin-bottom:8px">商品明細</p>
            <div
              v-for="item in drawerOrder.items"
              :key="item.orderItemId"
              style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid #f0f0f0"
            >
              <img
                v-if="item.productImage"
                :src="item.productImage"
                style="width:48px;height:48px;object-fit:cover;border-radius:4px;background:#f5f5f5"
              />
              <div style="flex:1;font-size:13px">
                <div>{{ item.productName }}</div>
                <div style="color:#aaa;font-size:12px">{{ item.sizeName }} × {{ item.quantity }}</div>
              </div>
              <div style="font-size:13px;font-weight:500">NT $ {{ item.subtotal.toLocaleString() }}</div>
            </div>
          </div>

          <!-- 金額合計 -->
          <div style="margin-top:12px;text-align:right;font-size:15px;font-weight:700">
            合計 NT $ {{ drawerOrder.totalAmount.toLocaleString() }}
          </div>

          <!-- 刪除訂單 -->
          <div style="margin-top:24px;padding-top:16px;border-top:1px solid #f0f0f0">
            <n-popconfirm
              @positive-click="handleDeleteFromDrawer"
              positive-text="確認刪除"
              negative-text="取消"
            >
              <template #trigger>
                <n-button type="error" ghost size="small" :loading="deleting">
                  刪除此訂單
                </n-button>
              </template>
              確定刪除訂單「{{ drawerOrder.orderNo }}」？此操作無法復原。
            </n-popconfirm>
          </div>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted, computed } from "vue";
import { NButton, NTag, NSpace, NPopconfirm, useMessage, type DataTableColumns } from "naive-ui";
import { orderApi, type AdminOrder } from "../../../services/order";

const message = useMessage();

/* ── 篩選 ── */
const filters = ref({
  keyword: "",
  status: undefined as string | undefined,
  dateRange: null as [number, number] | null,
});

const statusOptions = [
  { label: "待付款", value: "pending" },
  { label: "已付款", value: "PAID" },
  { label: "付款失敗", value: "FAILED" },
  { label: "已出貨", value: "SHIPPED" },
  { label: "已完成", value: "COMPLETED" },
  { label: "已取消", value: "CANCELLED" },
];

const statusConfig: Record<string, { type: string; label: string; color: string }> = {
  pending:   { type: "warning",  label: "待付款",  color: "#f0a020" },
  PAID:      { type: "success",  label: "已付款",  color: "#18a058" },
  FAILED:    { type: "error",    label: "付款失敗", color: "#d03050" },
  SHIPPED:   { type: "info",     label: "已出貨",  color: "#2080f0" },
  COMPLETED: { type: "success",  label: "已完成",  color: "#18a058" },
  CANCELLED: { type: "default",  label: "已取消",  color: "#aaa" },
};

/* ── 資料 ── */
const loading = ref(false);
const orders = ref<AdminOrder[]>([]);
const currentPage = ref(1);
const pageSize = 20;
const totalPages = ref(1);
const totalCount = ref(0);

/* ── 統計小卡 ── */
const statCounts = ref<Record<string, number>>({});

const statCards = computed(() => [
  { key: "pending",   label: "待付款",  color: "#f0a020" },
  { key: "PAID",      label: "已付款",  color: "#18a058" },
  { key: "SHIPPED",   label: "已出貨",  color: "#2080f0" },
  { key: "COMPLETED", label: "已完成",  color: "#5cb85c" },
  { key: "CANCELLED", label: "已取消",  color: "#aaa" },
].map(s => ({ ...s, count: statCounts.value[s.key] ?? 0 })));

const loadStats = async () => {
  for (const s of ["pending", "PAID", "SHIPPED", "COMPLETED", "CANCELLED"]) {
    try {
      const r = await orderApi.getOrders({ status: s, pageSize: 1 });
      statCounts.value[s] = r.total;
    } catch {}
  }
};

const loadOrders = async () => {
  loading.value = true;
  try {
    const params: any = {
      keyword: filters.value.keyword || undefined,
      status: filters.value.status || undefined,
      page: currentPage.value,
      pageSize,
    };
    if (filters.value.dateRange) {
      params.startDate = new Date(filters.value.dateRange[0]).toISOString().split("T")[0];
      params.endDate   = new Date(filters.value.dateRange[1]).toISOString().split("T")[0];
    }
    const res = await orderApi.getOrders(params);
    orders.value = res.items;
    totalPages.value = res.totalPages;
    totalCount.value = res.total;
  } catch (err: any) {
    message.error("載入訂單失敗：" + (err.message ?? ""));
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => { currentPage.value = 1; loadOrders(); };
const handleReset = () => {
  filters.value = { keyword: "", status: undefined, dateRange: null };
  currentPage.value = 1;
  loadOrders();
};
const quickFilter = (key: string) => {
  filters.value.status = filters.value.status === key ? undefined : key;
  handleSearch();
};

onMounted(() => { loadOrders(); loadStats(); });

/* ── 表格欄位 ── */
const columns: DataTableColumns<AdminOrder> = [
  { title: "訂單編號", key: "orderNo", width: 200,
    render: row => h("span", { style: "font-family:monospace;font-size:12px" }, row.orderNo) },
  { title: "收件人", key: "receiverName", width: 100 },
  { title: "品項數", key: "items", width: 72,
    render: row => h("span", {}, (row.items?.length ?? "-")) },
  { title: "訂單金額", key: "totalAmount", width: 110,
    render: row => `NT$ ${row.totalAmount.toLocaleString()}` },
  { title: "狀態", key: "status", width: 100,
    render: row => {
      const cfg = statusConfig[row.status] ?? { type: "default", label: row.status };
      return h(NTag, { type: cfg.type as any, size: "small", round: true }, () => cfg.label);
    }
  },
  { title: "付款", key: "paymentMethod", width: 100,
    render: row => paymentLabel(row.paymentMethod) },
  { title: "訂單時間", key: "createdAt", width: 155,
    render: row => formatDate(row.createdAt) },
  {
    title: "操作", key: "actions", width: 140, fixed: "right",
    render: row => h(NSpace, { size: 6 }, () => [
      h(NButton, { size: "small", onClick: () => openDrawer(row.orderNo) }, () => "查看"),
      h(NPopconfirm, {
        onPositiveClick: () => handleDeleteFromTable(row),
        positiveText: "確認刪除",
        negativeText: "取消",
      }, {
        trigger: () => h(NButton, { size: "small", type: "error", ghost: true }, () => "刪除"),
        default: () => `確定刪除訂單 ${row.orderNo}？`,
      }),
    ]),
  },
];

/* ── 詳情 Drawer ── */
const drawerOpen = ref(false);
const detailLoading = ref(false);
const drawerOrder = ref<AdminOrder | null>(null);
const editStatus = ref("");
const statusUpdating = ref(false);
const deleting = ref(false);

const openDrawer = async (orderNo: string) => {
  drawerOpen.value = true;
  detailLoading.value = true;
  drawerOrder.value = null;
  try {
    const order = await orderApi.getOrderDetail(orderNo);
    drawerOrder.value = order;
    editStatus.value = order.status;
  } catch (err: any) {
    message.error("載入訂單詳情失敗");
    drawerOpen.value = false;
  } finally {
    detailLoading.value = false;
  }
};

const handleUpdateStatus = async () => {
  if (!drawerOrder.value) return;
  statusUpdating.value = true;
  try {
    await orderApi.updateStatus(drawerOrder.value.orderNo, editStatus.value);
    drawerOrder.value.status = editStatus.value;
    // 同步更新列表中的該筆
    const row = orders.value.find(o => o.orderNo === drawerOrder.value!.orderNo);
    if (row) row.status = editStatus.value;
    message.success("狀態更新成功");
    loadStats();
  } catch (err: any) {
    message.error("更新失敗：" + (err.message ?? ""));
  } finally {
    statusUpdating.value = false;
  }
};

const handleDeleteFromTable = async (row: AdminOrder) => {
  try {
    await orderApi.deleteOrder(row.orderNo);
    orders.value = orders.value.filter(o => o.orderNo !== row.orderNo);
    message.success("訂單已刪除");
    loadStats();
  } catch (err: any) {
    message.error("刪除失敗：" + (err.message ?? ""));
  }
};

const handleDeleteFromDrawer = async () => {
  if (!drawerOrder.value) return;
  deleting.value = true;
  try {
    await orderApi.deleteOrder(drawerOrder.value.orderNo);
    orders.value = orders.value.filter(o => o.orderNo !== drawerOrder.value!.orderNo);
    drawerOpen.value = false;
    message.success("訂單已刪除");
    loadStats();
  } catch (err: any) {
    message.error("刪除失敗：" + (err.message ?? ""));
  } finally {
    deleting.value = false;
  }
};

/* ── 格式化 ── */
const formatDate = (d: string) => {
  if (!d) return "";
  return new Date(d).toLocaleString("zh-TW", {
    year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit",
  });
};
const paymentLabel = (m: string) =>
  ({ ecpay: "綠界金流", credit_card: "信用卡" }[m] ?? m ?? "-");
const shippingLabel = (m: string) =>
  ({ home: "宅配到府", store: "超商取貨" }[m] ?? m ?? "-");
</script>

<style scoped lang="scss">
.orders-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }
}

/* 統計小卡 */
.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 20px;
  border: 1.5px solid #ddd;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s, border-color 0.15s;
  background: #fff;

  &:hover { background: #f5f5f5; }
  &.active { background: #f0f0f0; }

  .stat-label { color: #555; }
  .stat-count { font-weight: 600; }
}
</style>
