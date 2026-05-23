<template>
  <div class="promotions-page">
    <n-card title="優惠活動" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">
          <template #icon>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
            >
              <path
                d="M12 4v16m8-8H4"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </template>
          新增活動
        </n-button>
      </template>

      <!-- 空狀態（待串接 API） -->
      <n-empty description="暫無優惠活動資料" style="padding: 48px 0" />
    </n-card>

    <!-- 新增彈窗 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      title="新增優惠活動"
      style="width: 600px"
    >
      <n-form label-placement="left" label-width="100">
        <n-form-item label="活動名稱" required>
          <n-input placeholder="例如：新春特惠" />
        </n-form-item>
        <n-form-item label="活動類型" required>
          <n-select :options="typeOptions" placeholder="選擇活動類型" />
        </n-form-item>
        <n-form-item label="優惠內容" required>
          <n-input placeholder="例如：全館 8 折" />
        </n-form-item>
        <n-form-item label="活動期間" required>
          <n-date-picker type="daterange" style="width: 100%" />
        </n-form-item>
        <n-form-item label="活動描述">
          <n-input type="textarea" :rows="3" placeholder="活動詳細說明" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary">建立</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

const showModal = ref(false);

const typeOptions = [
  { label: "全館折扣", value: "sitewide" },
  { label: "分類折扣", value: "category" },
  { label: "單品折扣", value: "product" },
  { label: "滿額折扣", value: "threshold" },
];
</script>

<style scoped lang="scss">
.promotions-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }
}
</style>
