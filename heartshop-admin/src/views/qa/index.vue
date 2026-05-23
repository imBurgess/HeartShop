<template>
  <div class="qa-page">
    <n-card :bordered="false" class="page-card">
      <template #header>
        <div class="page-header">
          <span class="page-title">問答管理</span>
          <n-tag :type="unansweredCount > 0 ? 'warning' : 'success'" size="small">
            {{ unansweredCount > 0 ? `${unansweredCount} 則待回覆` : '全部已回覆' }}
          </n-tag>
        </div>
      </template>

      <!-- 分頁標籤 -->
      <n-tabs v-model:value="activeTab" type="line" @update:value="onTabChange">
        <n-tab-pane name="product" tab="商品問答">
          <!-- 篩選列 -->
          <div class="filter-bar">
            <n-radio-group v-model:value="answeredFilter" size="small" @update:value="onFilterChange">
              <n-radio-button value="">全部</n-radio-button>
              <n-radio-button value="false">待回覆</n-radio-button>
              <n-radio-button value="true">已回覆</n-radio-button>
            </n-radio-group>
          </div>

          <n-data-table
            :columns="productColumns"
            :data="productItems"
            :loading="loading"
            :pagination="false"
            :bordered="false"
            size="small"
            class="qa-table"
          />

          <div class="pagination-bar">
            <n-pagination
              v-model:page="page"
              :page-count="Math.ceil(total / pageSize)"
              :page-size="pageSize"
              show-quick-jumper
              @update:page="loadData"
            />
          </div>
        </n-tab-pane>

        <n-tab-pane name="order" tab="訂單問答">
          <div class="filter-bar">
            <n-radio-group v-model:value="answeredFilter" size="small" @update:value="onFilterChange">
              <n-radio-button value="">全部</n-radio-button>
              <n-radio-button value="false">待回覆</n-radio-button>
              <n-radio-button value="true">已回覆</n-radio-button>
            </n-radio-group>
          </div>

          <n-data-table
            :columns="orderColumns"
            :data="orderItems"
            :loading="loading"
            :pagination="false"
            :bordered="false"
            size="small"
            class="qa-table"
          />

          <div class="pagination-bar">
            <n-pagination
              v-model:page="page"
              :page-count="Math.ceil(total / pageSize)"
              :page-size="pageSize"
              show-quick-jumper
              @update:page="loadData"
            />
          </div>
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <!-- 回覆 Modal -->
    <n-modal
      v-model:show="replyModalVisible"
      preset="card"
      title="回覆問題"
      style="width: 560px"
      :segmented="{ content: true, footer: true }"
    >
      <div v-if="currentQa" class="reply-question-block">
        <div class="rq-badge">Q</div>
        <div class="rq-body">
          <p class="rq-text">{{ currentQa.question }}</p>
          <span class="rq-meta">
            {{ currentQa.memberName || '匿名' }} ·
            {{ activeTab === 'product' ? (currentQa.productName || '商品') : `訂單 ${currentQa.orderNo}` }} ·
            {{ formatDate(currentQa.createdAt) }}
          </span>
        </div>
      </div>

      <n-divider style="margin: 12px 0" />

      <n-input
        v-model:value="replyText"
        type="textarea"
        placeholder="請輸入回覆內容..."
        :rows="4"
        :maxlength="1000"
        show-count
      />

      <template #footer>
        <n-space justify="end">
          <n-button @click="replyModalVisible = false">取消</n-button>
          <n-button
            type="primary"
            :loading="replying"
            :disabled="!replyText.trim()"
            @click="submitReply"
          >
            送出回覆
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from "vue";
import { NButton, NTag, NPopconfirm, NSpace, useMessage, type DataTableColumns } from "naive-ui";
import { qaApi, type AdminQaItem } from "../../../services/qa";

const message = useMessage();

const activeTab = ref<"product" | "order">("product");
const answeredFilter = ref<string>("");
const page = ref(1);
const pageSize = 20;
const total = ref(0);
const loading = ref(false);

const productItems = ref<AdminQaItem[]>([]);
const orderItems = ref<AdminQaItem[]>([]);

const unansweredCount = computed(() =>
  [...productItems.value, ...orderItems.value].filter((i) => !i.answer).length
);

const formatDate = (d: string) =>
  d ? new Date(d).toLocaleDateString("zh-TW") : "";

// ── 回覆 Modal ────────────────────────────────────────
const replyModalVisible = ref(false);
const currentQa = ref<AdminQaItem | null>(null);
const replyText = ref("");
const replying = ref(false);

const openReply = (row: AdminQaItem) => {
  currentQa.value = row;
  replyText.value = row.answer || "";
  replyModalVisible.value = true;
};

const submitReply = async () => {
  if (!currentQa.value || !replyText.value.trim()) return;
  replying.value = true;
  try {
    if (activeTab.value === "product") {
      await qaApi.answerProductQa(currentQa.value.qaId, replyText.value.trim());
      const idx = productItems.value.findIndex((i) => i.qaId === currentQa.value!.qaId);
      if (idx !== -1) productItems.value[idx].answer = replyText.value.trim();
    } else {
      await qaApi.answerOrderQa(currentQa.value.qaId, replyText.value.trim());
      const idx = orderItems.value.findIndex((i) => i.qaId === currentQa.value!.qaId);
      if (idx !== -1) orderItems.value[idx].answer = replyText.value.trim();
    }
    message.success("回覆成功");
    replyModalVisible.value = false;
  } catch (err: any) {
    message.error(err.message || "回覆失敗");
  } finally {
    replying.value = false;
  }
};

// ── 刪除 ────────────────────────────────────────────
const deleteQa = async (row: AdminQaItem) => {
  try {
    if (activeTab.value === "product") {
      await qaApi.deleteProductQa(row.qaId);
      productItems.value = productItems.value.filter((i) => i.qaId !== row.qaId);
      total.value = Math.max(0, total.value - 1);
    } else {
      await qaApi.deleteOrderQa(row.qaId);
      orderItems.value = orderItems.value.filter((i) => i.qaId !== row.qaId);
      total.value = Math.max(0, total.value - 1);
    }
    message.success("刪除成功");
  } catch (err: any) {
    message.error(err.message || "刪除失敗");
  }
};

// ── 資料載入 ──────────────────────────────────────────
const loadData = async () => {
  loading.value = true;
  try {
    const answered = answeredFilter.value === "" ? null
      : answeredFilter.value === "true";

    if (activeTab.value === "product") {
      const res = await qaApi.getProductQa({ page: page.value, pageSize, answered });
      productItems.value = res.items;
      total.value = res.total;
    } else {
      const res = await qaApi.getOrderQa({ page: page.value, pageSize, answered });
      orderItems.value = res.items;
      total.value = res.total;
    }
  } catch (err: any) {
    message.error(err.message || "載入失敗");
  } finally {
    loading.value = false;
  }
};

const onTabChange = () => {
  page.value = 1;
  loadData();
};

const onFilterChange = () => {
  page.value = 1;
  loadData();
};

// ── 表格欄位 ──────────────────────────────────────────
const productColumns: DataTableColumns<AdminQaItem> = [
  {
    title: "會員",
    key: "memberName",
    width: 90,
    render: (row) => row.memberName || "匿名",
  },
  {
    title: "商品",
    key: "productName",
    width: 130,
    ellipsis: { tooltip: true },
    render: (row) => row.productName || "-",
  },
  {
    title: "問題",
    key: "question",
    ellipsis: { tooltip: true },
  },
  {
    title: "回覆",
    key: "answer",
    ellipsis: { tooltip: true },
    render: (row) =>
      row.answer
        ? h("span", { style: "color:#555" }, row.answer)
        : h("span", { style: "color:#aaa" }, "尚未回覆"),
  },
  {
    title: "狀態",
    key: "status",
    width: 90,
    render: (row) =>
      h(NTag, { type: row.answer ? "success" : "warning", size: "small" },
        { default: () => (row.answer ? "已回覆" : "待回覆") }),
  },
  {
    title: "提問時間",
    key: "createdAt",
    width: 100,
    render: (row) => formatDate(row.createdAt),
  },
  {
    title: "操作",
    key: "action",
    width: 140,
    render: (row) =>
      h(NSpace, { size: 6 }, {
        default: () => [
          h(NButton, { size: "small", type: "primary", ghost: true, onClick: () => openReply(row) },
            { default: () => (row.answer ? "修改回覆" : "回覆") }),
          h(NPopconfirm, { onPositiveClick: () => deleteQa(row) }, {
            trigger: () => h(NButton, { size: "small", type: "error", ghost: true }, { default: () => "刪除" }),
            default: () => "確定要刪除此問答？",
          }),
        ],
      }),
  },
];

const orderColumns: DataTableColumns<AdminQaItem> = [
  {
    title: "會員",
    key: "memberName",
    width: 90,
    render: (row) => row.memberName || "匿名",
  },
  {
    title: "訂單編號",
    key: "orderNo",
    width: 160,
    ellipsis: { tooltip: true },
  },
  {
    title: "問題",
    key: "question",
    ellipsis: { tooltip: true },
  },
  {
    title: "回覆",
    key: "answer",
    ellipsis: { tooltip: true },
    render: (row) =>
      row.answer
        ? h("span", { style: "color:#555" }, row.answer)
        : h("span", { style: "color:#aaa" }, "尚未回覆"),
  },
  {
    title: "狀態",
    key: "status",
    width: 90,
    render: (row) =>
      h(NTag, { type: row.answer ? "success" : "warning", size: "small" },
        { default: () => (row.answer ? "已回覆" : "待回覆") }),
  },
  {
    title: "提問時間",
    key: "createdAt",
    width: 100,
    render: (row) => formatDate(row.createdAt),
  },
  {
    title: "操作",
    key: "action",
    width: 140,
    render: (row) =>
      h(NSpace, { size: 6 }, {
        default: () => [
          h(NButton, { size: "small", type: "primary", ghost: true, onClick: () => openReply(row) },
            { default: () => (row.answer ? "修改回覆" : "回覆") }),
          h(NPopconfirm, { onPositiveClick: () => deleteQa(row) }, {
            trigger: () => h(NButton, { size: "small", type: "error", ghost: true }, { default: () => "刪除" }),
            default: () => "確定要刪除此問答？",
          }),
        ],
      }),
  },
];

onMounted(() => loadData());
</script>

<style scoped lang="scss">
.qa-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 1rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.filter-bar {
  margin-bottom: 16px;
}

.qa-table {
  min-height: 200px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 回覆 Modal — 問題區塊 */
.reply-question-block {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--color-gray-50);
  border-radius: var(--radius);
}

.rq-badge {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-gray-700);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rq-body {
  flex: 1;
}

.rq-text {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--color-gray-900);
  line-height: 1.5;
}

.rq-meta {
  font-size: 12px;
  color: var(--color-gray-500);
}
</style>
