<template>
  <div class="inventory-page">
    <div class="page-header">
      <h1 class="page-title">庫存管理</h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜尋商品編號或名稱"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon class="el-icon--left"><Search /></el-icon>
          搜尋
        </el-button>
        <el-button @click="refreshData">
          <el-icon class="el-icon--left"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 低庫存警示 -->
    <el-alert
      v-if="lowStockCount > 0"
      :title="`目前有 ${lowStockCount} 項商品庫存不足！`"
      type="warning"
      :closable="false"
      show-icon
    >
      <el-button link type="warning" @click="filterLowStock"
        >查看詳情</el-button
      >
    </el-alert>

    <!-- 篩選器 -->
    <el-card class="filter-card" shadow="never">
      <el-checkbox
        v-model="queryParams.lowStockOnly"
        @change="handleQueryChange"
      >
        只顯示低庫存商品
      </el-checkbox>
    </el-card>

    <!-- 庫存列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="inventoryList"
        v-loading="loading"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="code" label="商品編號" width="120" />
        <el-table-column prop="name" label="商品名稱" min-width="200" />
        <el-table-column prop="categoryName" label="分類" width="120" />

        <el-table-column label="當前庫存" width="180">
          <template #default="{ row }">
            <div class="stock-cell">
              <span :class="{ 'low-stock': row.isLowStock }">
                {{ row.stockQuantity }}
              </span>
              <el-progress
                :percentage="getStockPercentage(row)"
                :color="getStockColor(row)"
                :show-text="false"
                style="width: 80px; margin-left: 10px"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="stockAlertThreshold"
          label="警戒值"
          width="100"
        />

        <el-table-column label="狀態" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isLowStock" type="warning" size="small"
              >低庫存</el-tag
            >
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdjustStock(row)">
              調整庫存
            </el-button>
            <el-button link type="info" @click="handleViewLogs(row)">
              查看記錄
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 調整庫存對話框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="調整庫存"
      width="500px"
      @close="resetAdjustForm"
    >
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="商品名稱">
          <el-text>{{ currentProduct?.name }}</el-text>
        </el-form-item>
        <el-form-item label="商品編號">
          <el-text>{{ currentProduct?.code }}</el-text>
        </el-form-item>
        <el-form-item label="當前庫存">
          <el-text>{{ currentProduct?.stockQuantity }}</el-text>
        </el-form-item>
        <el-form-item label="調整數量" required>
          <el-input-number
            v-model="adjustForm.quantityChange"
            :min="-currentProduct?.stockQuantity || -999"
            :max="9999"
            placeholder="正數增加，負數減少"
          />
        </el-form-item>
        <el-form-item label="操作人員">
          <el-input v-model="adjustForm.operator" placeholder="選填" />
        </el-form-item>
        <el-form-item label="備註說明">
          <el-input
            v-model="adjustForm.remark"
            type="textarea"
            :rows="3"
            placeholder="填寫調整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirmAdjust"
          :loading="adjusting"
        >
          確認調整
        </el-button>
      </template>
    </el-dialog>

    <!-- 異動記錄對話框 -->
    <el-dialog v-model="logsDialogVisible" title="庫存異動記錄" width="800px">
      <el-table :data="inventoryLogs" v-loading="logsLoading">
        <el-table-column prop="changeTypeDesc" label="異動類型" width="100" />
        <el-table-column label="異動數量" width="100">
          <template #default="{ row }">
            <span
              :class="row.quantityChange > 0 ? 'text-success' : 'text-danger'"
            >
              {{ row.quantityChange > 0 ? "+" : "" }}{{ row.quantityChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="quantityBefore" label="異動前" width="80" />
        <el-table-column prop="quantityAfter" label="異動後" width="80" />
        <el-table-column prop="operator" label="操作人員" width="100" />
        <el-table-column prop="remark" label="備註" min-width="150" />
        <el-table-column prop="createdAt" label="異動時間" width="160" />
      </el-table>
      <div class="pagination" style="margin-top: 20px">
        <el-pagination
          v-model:current-page="logsPage"
          v-model:page-size="logsPageSize"
          :total="logsTotal"
          layout="total, prev, pager, next"
          @current-change="loadInventoryLogs"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { Search, Refresh } from "@element-plus/icons-vue";
import {
  inventoryApi,
  type InventoryQueryParams,
  type InventoryAdjustRequest,
} from "../../services/inventory";
import type { Product } from "../../services/inventory";

// 狀態管理
const loading = ref(false);
const adjusting = ref(false);
const logsLoading = ref(false);
const inventoryList = ref<Product[]>([]);
const inventoryLogs = ref<any[]>([]);
const total = ref(0);
const searchKeyword = ref("");
const lowStockCount = ref(0);

// 查詢參數
const queryParams = reactive<InventoryQueryParams>({
  page: 1,
  pageSize: 20,
  lowStockOnly: false,
  sortBy: "stock",
  sortOrder: "asc",
});

// 調整庫存對話框
const adjustDialogVisible = ref(false);
const currentProduct = ref<Product | null>(null);
const adjustForm = reactive<InventoryAdjustRequest>({
  quantityChange: 0,
  operator: "",
  remark: "",
});

// 異動記錄對話框
const logsDialogVisible = ref(false);
const logsPage = ref(1);
const logsPageSize = ref(10);
const logsTotal = ref(0);

// 載入庫存列表
const loadInventoryList = async () => {
  loading.value = true;
  try {
    const params = { ...queryParams };
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }

    const response = await inventoryApi.getInventoryList(params);
    inventoryList.value = response.items;
    total.value = response.total;
  } catch (error) {
    console.error("Failed to load inventory:", error);
    ElMessage.error("載入庫存列表失敗");
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

// 取得庫存狀態顏色
const getStockColor = (row: Product) => {
  return row.isLowStock ? "#f56c6c" : "#67c23a";
};

// 搜尋處理
const handleSearch = () => {
  queryParams.page = 1;
  loadInventoryList();
};

// 刷新數據
const refreshData = () => {
  loadInventoryList();
  loadLowStockAlerts();
};

// 篩選低庫存
const filterLowStock = () => {
  queryParams.lowStockOnly = true;
  queryParams.page = 1;
  loadInventoryList();
};

// 查詢參數變更
const handleQueryChange = () => {
  queryParams.page = 1;
  loadInventoryList();
};

// 分頁變更
const handlePageChange = (page: number) => {
  queryParams.page = page;
  loadInventoryList();
};

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size;
  queryParams.page = 1;
  loadInventoryList();
};

// 排序變更
const handleSortChange = ({ prop, order }: any) => {
  if (order) {
    queryParams.sortBy = prop;
    queryParams.sortOrder = order === "ascending" ? "asc" : "desc";
  } else {
    queryParams.sortBy = undefined;
    queryParams.sortOrder = undefined;
  }
  loadInventoryList();
};

// 調整庫存
const handleAdjustStock = (row: Product) => {
  currentProduct.value = row;
  adjustDialogVisible.value = true;
};

// 確認調整
const handleConfirmAdjust = async () => {
  if (!currentProduct.value || adjustForm.quantityChange === 0) {
    ElMessage.warning("請輸入調整數量");
    return;
  }

  adjusting.value = true;
  try {
    await inventoryApi.adjustStock(currentProduct.value.productId, adjustForm);
    ElMessage.success("庫存調整成功");
    adjustDialogVisible.value = false;
    refreshData();
  } catch (error) {
    console.error("Failed to adjust stock:", error);
    ElMessage.error("庫存調整失敗");
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
  logsPage.value = 1;
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
      logsPage.value,
      logsPageSize.value
    );
    inventoryLogs.value = response.items;
    logsTotal.value = response.total;
  } catch (error) {
    console.error("Failed to load logs:", error);
    ElMessage.error("載入異動記錄失敗");
  } finally {
    logsLoading.value = false;
  }
};

// 初始化
onMounted(() => {
  loadInventoryList();
  loadLowStockAlerts();
});
</script>

<style scoped>
.inventory-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
  padding: 10px 20px;
}

.table-card {
  margin-bottom: 20px;
}

.stock-cell {
  display: flex;
  align-items: center;
}

.low-stock {
  color: #f56c6c;
  font-weight: 600;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
