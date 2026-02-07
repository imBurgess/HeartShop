<template>
  <div class="inventory-page">
    <n-card title="庫存管理" :bordered="false">
      <!-- 警告提示 -->
      <n-alert
        type="warning"
        style="margin-bottom: 16px"
        v-if="lowStockCount > 0"
      >
        <template #icon>
          <svg
            viewBox="0 0 24 24"
            fill="none"
            style="width: 20px; height: 20px"
          >
            <path
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
        </template>
        有 {{ lowStockCount }} 項商品庫存不足，請盡快補貨！
        <n-button
          text
          type="warning"
          @click="
            showLowStockOnly = true;
            loadData();
          "
        >
          查看詳情
        </n-button>
      </n-alert>

      <!-- 篩選區 -->
      <n-space :size="12" style="margin-bottom: 16px">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜尋商品名稱或編號"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />

        <n-checkbox
          v-model:checked="showLowStockOnly"
          @update:checked="handleSearch"
        >
          只顯示低庫存商品
        </n-checkbox>

        <n-button type="primary" @click="handleSearch">搜尋</n-button>
        <n-button @click="refreshData">刷新</n-button>
      </n-space>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="inventoryList"
        :loading="loading"
        :row-key="(row: any) => row.productId"
        :pagination="paginationReactive"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 調整庫存對話框 -->
    <n-modal
      v-model:show="adjustDialogVisible"
      preset="dialog"
      title="調整庫存"
      :show-icon="false"
      style="width: 500px"
    >
      <n-form :model="adjustForm" label-placement="left" label-width="100px">
        <n-form-item label="商品名稱">
          <n-text>{{ currentProduct?.name }}</n-text>
        </n-form-item>
        <n-form-item label="商品編號">
          <n-text>{{ currentProduct?.code }}</n-text>
        </n-form-item>
        <n-form-item label="當前庫存">
          <n-text>{{ currentProduct?.stockQuantity }}</n-text>
        </n-form-item>
        <n-form-item label="調整數量" required>
          <n-input-number
            v-model:value="adjustForm.quantityChange"
            :min="-currentProduct?.stockQuantity || -999"
            :max="9999"
            placeholder="正數增加，負數減少"
          />
        </n-form-item>
        <n-form-item label="操作人員">
          <n-input v-model:value="adjustForm.operator" placeholder="選填" />
        </n-form-item>
        <n-form-item label="備註說明">
          <n-input
            v-model:value="adjustForm.remark"
            type="textarea"
            :rows="3"
            placeholder="填寫調整原因"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="adjustDialogVisible = false">取消</n-button>
          <n-button
            type="primary"
            @click="handleConfirmAdjust"
            :loading="adjusting"
          >
            確認調整
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 異動記錄對話框 -->
    <n-modal
      v-model:show="logsDialogVisible"
      preset="card"
      title="庫存異動記錄"
      style="width: 800px"
    >
      <n-data-table
        :columns="logColumns"
        :data="inventoryLogs"
        :loading="logsLoading"
        :pagination="logsPagination"
        @update:page="loadInventoryLogs"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, computed, onMounted } from "vue";
import {
  NButton,
  NTag,
  NSpace,
  NProgress,
  NText,
  useMessage,
  type DataTableColumns,
  type PaginationProps,
} from "naive-ui";
import { inventoryApi } from "../../../services/inventory";
import type { Product } from "../../../services/inventory";

// 狀態管理
const message = useMessage();
const loading = ref(false);
const adjusting = ref(false);
const logsLoading = ref(false);
const inventoryList = ref<Product[]>([]);
const inventoryLogs = ref<any[]>([]);
const searchKeyword = ref("");
const showLowStockOnly = ref(false);
const lowStockCount = ref(0);

// 分頁
const paginationReactive = reactive<PaginationProps>({
  page: 1,
  pageSize: 20,
  pageSizes: [10, 20, 50, 100],
  showSizePicker: true,
  itemCount: 0,
});

// 調整庫存對話框
const adjustDialogVisible = ref(false);
const currentProduct = ref<Product | null>(null);
const adjustForm = reactive({
  quantityChange: 0,
  operator: "",
  remark: "",
});

// 異動記錄對話框
const logsDialogVisible = ref(false);
const logsPagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
});

// 載入資料
const loadData = async () => {
  loading.value = true;
  try {
    const response = await inventoryApi.getInventoryList({
      keyword: searchKeyword.value || undefined,
      lowStockOnly: showLowStockOnly.value || undefined,
      page: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
    });
    inventoryList.value = response.items;
    paginationReactive.itemCount = response.total;
  } catch (error) {
    console.error("Failed to load inventory:", error);
    message.error("載入庫存列表失敗");
  } finally {
    loading.value = false;
  }
};

// 載入低庫存警示
const loadLowStockAlerts = async () => {
  try {
    const alerts = await inventoryApi.getLowStockAlerts();
    lowStockCount.value = alerts.length;
  } catch (error) {
    console.error("Failed to load alerts:", error);
  }
};

// 計算庫存百分比
const getStockPercentage = (row: Product) => {
  const stock = row.stockQuantity || 0;
  const threshold = row.stockAlertThreshold || 10;
  const max = threshold * 2;
  return Math.min((stock / max) * 100, 100);
};

// 取得庫存狀態
const getStockStatus = (row: Product) => {
  if (row.isLowStock) {
    return { type: "warning", label: "低庫存" };
  }
  return { type: "success", label: "正常" };
};

// 表格欄位
const columns: DataTableColumns<Product> = [
  {
    title: "商品編號",
    key: "code",
    width: 120,
  },
  {
    title: "商品名稱",
    key: "name",
    width: 200,
  },
  {
    title: "分類",
    key: "categoryName",
    width: 120,
  },
  {
    title: "當前庫存",
    key: "stockQuantity",
    width: 120,
    render: (row) => {
      const status = getStockStatus(row);
      return h(
        "div",
        { style: "display: flex; align-items: center; gap: 8px" },
        [
          h(
            "span",
            {
              style: `font-weight: 600; color: ${
                row.isLowStock ? "#f56c6c" : "inherit"
              }`,
            },
            row.stockQuantity
          ),
          h(
            NTag,
            { type: status.type as any, size: "small" },
            () => status.label
          ),
        ]
      );
    },
  },
  {
    title: "庫存率",
    key: "stockLevel",
    width: 180,
    render: (row) => {
      const percentage = getStockPercentage(row);
      return h("div", [
        h(NProgress, {
          type: "line",
          percentage,
          status: row.isLowStock ? "warning" : "success",
          showIndicator: false,
        }),
      ]);
    },
  },
  {
    title: "警戒值",
    key: "stockAlertThreshold",
    width: 100,
  },
  {
    title: "操作",
    key: "actions",
    width: 180,
    fixed: "right",
    render: (row) => {
      return h(NSpace, { size: 8 }, () => [
        h(
          NButton,
          {
            size: "small",
            type: "primary",
            onClick: () => handleAdjustStock(row),
          },
          () => "調整庫存"
        ),
        h(
          NButton,
          {
            size: "small",
            onClick: () => handleViewLogs(row),
          },
          () => "查看記錄"
        ),
      ]);
    },
  },
];

// 異動記錄表格
const logColumns: DataTableColumns<any> = [
  {
    title: "異動類型",
    key: "changeTypeDesc",
    width: 100,
  },
  {
    title: "異動數量",
    key: "quantityChange",
    width: 100,
    render: (row) => {
      const isPositive = row.quantityChange > 0;
      return h(
        "span",
        {
          style: `color: ${
            isPositive ? "#67c23a" : "#f56c6c"
          }; font-weight: 600;`,
        },
        `${isPositive ? "+" : ""}${row.quantityChange}`
      );
    },
  },
  {
    title: "異動前",
    key: "quantityBefore",
    width: 80,
  },
  {
    title: "異動後",
    key: "quantityAfter",
    width: 80,
  },
  {
    title: "操作人員",
    key: "operator",
    width: 100,
  },
  {
    title: "備註",
    key: "remark",
    minWidth: 150,
  },
  {
    title: "異動時間",
    key: "createdAt",
    width: 160,
  },
];

// 搜尋處理
const handleSearch = () => {
  paginationReactive.page = 1;
  loadData();
};

// 刷新資料
const refreshData = () => {
  loadData();
  loadLowStockAlerts();
};

// 分頁變更
const handlePageChange = (page: number) => {
  paginationReactive.page = page;
  loadData();
};

const handlePageSizeChange = (pageSize: number) => {
  paginationReactive.pageSize = pageSize;
  paginationReactive.page = 1;
  loadData();
};

// 調整庫存
const handleAdjustStock = (row: Product) => {
  currentProduct.value = row;
  adjustDialogVisible.value = true;
};

// 確認調整
const handleConfirmAdjust = async () => {
  if (!currentProduct.value || adjustForm.quantityChange === 0) {
    message.warning("請輸入調整數量");
    return;
  }

  adjusting.value = true;
  try {
    await inventoryApi.adjustStock(currentProduct.value.productId, adjustForm);
    message.success("庫存調整成功");
    adjustDialogVisible.value = false;
    resetAdjustForm();
    refreshData();
  } catch (error) {
    console.error("Failed to adjust stock:", error);
    message.error("庫存調整失敗");
  } finally {
    adjusting.value = false;
  }
};

// 重置調整表單
const resetAdjustForm = () => {
  adjustForm.quantityChange = 0;
  adjustForm.operator = "";
  adjustForm.remark = "";
  currentProduct.value = null;
};

// 查看異動記錄
const handleViewLogs = (row: Product) => {
  currentProduct.value = row;
  logsPagination.page = 1;
  logsDialogVisible.value = true;
  loadInventoryLogs();
};

// 載入異動記錄
const loadInventoryLogs = async () => {
  if (!currentProduct.value) return;

  logsLoading.value = true;
  try {
    const response = await inventoryApi.getInventoryLogs(
      currentProduct.value.productId,
      logsPagination.page || 1,
      logsPagination.pageSize || 10
    );
    inventoryLogs.value = response.items;
    logsPagination.itemCount = response.total;
  } catch (error) {
    console.error("Failed to load logs:", error);
    message.error("載入異動記錄失敗");
  } finally {
    logsLoading.value = false;
  }
};

// 初始化
onMounted(() => {
  loadData();
  loadLowStockAlerts();
});
</script>

<style scoped lang="scss">
.inventory-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }
}
</style>
