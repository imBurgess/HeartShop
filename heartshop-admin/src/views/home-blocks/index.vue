<template>
  <div class="home-blocks-page">
    <n-card title="首頁區塊管理" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="handleCreate">
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
          新增區塊
        </n-button>
      </template>

      <!-- 類型篩選 -->
      <n-space style="margin-bottom: 16px">
        <n-select
          v-model:value="filterType"
          :options="typeOptions"
          placeholder="篩選類型"
          clearable
          style="width: 200px"
          @update:value="loadBlocks"
        />
        <n-button @click="loadBlocks" type="primary">重新載入</n-button>
      </n-space>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="blocks"
        :loading="loading"
        :row-key="(row: any) => row.blockId"
        :pagination="{ pageSize: 20 }"
        style="margin-top: 16px"
      />
    </n-card>

    <!-- 新增/編輯彈窗 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="isEdit ? '編輯區塊' : '新增區塊'"
      style="width: 700px"
    >
      <n-form :model="formData" label-placement="left" label-width="120">
        <n-form-item label="類型" required>
          <n-select
            v-model:value="formData.type"
            :options="typeOptions"
            placeholder="選擇類型"
          />
        </n-form-item>

        <n-form-item label="標題">
          <n-input
            v-model:value="formData.title"
            placeholder="請輸入標題（公告類型建議填寫）"
          />
        </n-form-item>

        <n-form-item label="副標題">
          <n-input
            v-model:value="formData.subtitle"
            placeholder="請輸入副標題（選填）"
          />
        </n-form-item>

        <n-form-item label="圖片" required>
          <n-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :max="1"
            list-type="image-card"
            :default-file-list="imageFileList"
            @finish="handleUploadFinish"
            @remove="handleImageRemove"
          >
            <n-button v-if="!formData.imageUrl">上傳圖片</n-button>
          </n-upload>
          <n-text
            v-if="formData.imageUrl"
            depth="3"
            style="margin-top: 8px; display: block"
          >
            當前圖片: {{ formData.imageUrl }}
          </n-text>
        </n-form-item>

        <n-form-item label="連結 URL">
          <n-input
            v-model:value="formData.linkUrl"
            placeholder="點擊後的跳轉連結"
          />
        </n-form-item>

        <n-form-item label="排序">
          <n-input-number
            v-model:value="formData.sortOrder"
            :min="0"
            style="width: 100%"
            placeholder="數字越小越前面"
          />
        </n-form-item>

        <n-form-item label="狀態">
          <n-checkbox v-model:checked="formData.isActive">啟用</n-checkbox>
        </n-form-item>

        <n-form-item label="開始時間">
          <n-date-picker
            v-model:value="startTimeValue"
            type="datetime"
            clearable
            style="width: 100%"
            placeholder="不設定表示立即生效"
          />
        </n-form-item>

        <n-form-item label="結束時間">
          <n-date-picker
            v-model:value="endTimeValue"
            type="datetime"
            clearable
            style="width: 100%"
            placeholder="不設定表示永久有效"
          />
        </n-form-item>

        <!-- 商品選擇（僅 PRODUCT_RECOMMEND 顯示） -->
        <n-form-item
          v-if="formData.type === 'PRODUCT_RECOMMEND'"
          label="關聯商品"
        >
          <n-input
            v-model:value="productIdsInput"
            placeholder="輸入商品 ID，多個用逗號分隔（例如：1,2,3）"
          />
          <n-text depth="3" style="font-size: 12px; margin-top: 4px">
            第一個為主圖，後續為子商品
          </n-text>
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            儲存
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, computed, onMounted } from "vue";
import {
  NButton,
  NTag,
  NSpace,
  NText,
  useDialog,
  useMessage,
  type DataTableColumns,
} from "naive-ui";
import * as homeBlockApi from "@/api/homeBlock";

const dialog = useDialog();
const message = useMessage();

const loading = ref(false);
const submitting = ref(false);
const blocks = ref<homeBlockApi.HomeBlockWithProducts[]>([]);
const showModal = ref(false);
const isEdit = ref(false);
const filterType = ref<string | null>(null);

const formData = ref({
  blockId: undefined as number | undefined,
  type: "CAROUSEL",
  title: "",
  subtitle: "",
  imageUrl: "",
  linkUrl: "",
  sortOrder: 0,
  isActive: true,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined,
});

const productIdsInput = ref("");

// 圖片上傳相關
const imageFileList = ref<any[]>([]);
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${baseUrl}/api/upload/image`;
});
const uploadHeaders = computed(() => {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
});

// 時間戳轉換
const startTimeValue = computed({
  get: () =>
    formData.value.startTime
      ? new Date(formData.value.startTime).getTime()
      : null,
  set: (val) => {
    formData.value.startTime = val ? new Date(val).toISOString() : undefined;
  },
});

const endTimeValue = computed({
  get: () =>
    formData.value.endTime ? new Date(formData.value.endTime).getTime() : null,
  set: (val) => {
    formData.value.endTime = val ? new Date(val).toISOString() : undefined;
  },
});

// 類型選項
const typeOptions = [
  { label: "輪播廣告", value: "CAROUSEL" },
  { label: "會員橫幅", value: "MEMBER_BANNER" },
  { label: "商品推薦", value: "PRODUCT_RECOMMEND" },
  { label: "一般公告", value: "GENERAL_ANNOUNCEMENT" },
];

// 表格欄位
const columns: DataTableColumns<any> = [
  { title: "ID", key: "blockId", width: 80 },
  {
    title: "類型",
    key: "type",
    width: 120,
    render: (row) => {
      const typeMap: Record<string, { text: string; type: any }> = {
        CAROUSEL: { text: "輪播廣告", type: "info" },
        MEMBER_BANNER: { text: "會員橫幅", type: "success" },
        PRODUCT_RECOMMEND: { text: "商品推薦", type: "warning" },
        GENERAL_ANNOUNCEMENT: { text: "一般公告", type: "default" },
      };
      const config = typeMap[row.type] || { text: row.type, type: "default" };
      return h(NTag, { type: config.type, size: "small" }, () => config.text);
    },
  },
  { title: "標題", key: "title", width: 200, ellipsis: { tooltip: true } },
  { title: "排序", key: "sortOrder", width: 80 },
  {
    title: "狀態",
    key: "isActive",
    width: 80,
    render: (row) => {
      return h(
        NTag,
        { type: row.isActive ? "success" : "default", size: "small" },
        () => (row.isActive ? "啟用" : "停用")
      );
    },
  },
  {
    title: "有效期間",
    key: "period",
    width: 180,
    render: (row) => {
      if (!row.startTime && !row.endTime) {
        return h(NText, { depth: 3 }, () => "永久有效");
      }
      const start = row.startTime
        ? new Date(row.startTime).toLocaleDateString()
        : "不限";
      const end = row.endTime
        ? new Date(row.endTime).toLocaleDateString()
        : "不限";
      return h(NText, { depth: 2 }, () => `${start} ~ ${end}`);
    },
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
          { size: "small", onClick: () => handleEdit(row) },
          () => "編輯"
        ),
        h(
          NButton,
          {
            size: "small",
            type: "error",
            onClick: () => handleDelete(row.blockId),
          },
          () => "刪除"
        ),
      ]);
    },
  },
];

// 載入區塊列表
const loadBlocks = async () => {
  loading.value = true;
  try {
    const response = await homeBlockApi.getAllBlocks();
    blocks.value = response.data;

    // 篩選類型
    if (filterType.value) {
      blocks.value = blocks.value.filter((b) => b.type === filterType.value);
    }
  } catch (error) {
    message.error("載入失敗");
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 新增
const handleCreate = () => {
  isEdit.value = false;
  formData.value = {
    blockId: undefined,
    type: "CAROUSEL",
    title: "",
    subtitle: "",
    imageUrl: "",
    linkUrl: "",
    sortOrder: 0,
    isActive: true,
    startTime: undefined,
    endTime: undefined,
  };
  productIdsInput.value = "";
  imageFileList.value = []; // 清空圖片列表
  showModal.value = true;
};

// 圖片上傳完成
const handleUploadFinish = ({ event }: any) => {
  const responseText = (event?.target as XMLHttpRequest)?.response || "{}";
  try {
    const response = JSON.parse(responseText);
    if (response.code === "0000" && response.data && response.data.url) {
      formData.value.imageUrl = response.data.url;
      message.success("圖片上傳成功");
    } else {
      console.error("圖片上傳回應格式不符:", response);
      message.error(response.message || "圖片上傳失敗");
    }
  } catch (error) {
    console.error("解析上傳回應失敗:", error);
    message.error("圖片上傳失敗：無法解析伺服器回應");
  }
};

// 移除圖片
const handleImageRemove = () => {
  formData.value.imageUrl = "";
  imageFileList.value = [];
};

// 編輯
const handleEdit = (row: any) => {
  isEdit.value = true;
  formData.value = { ...row };

  // 如果有商品關聯，轉成字串
  if (row.products && row.products.length > 0) {
    productIdsInput.value = row.products.map((p: any) => p.productId).join(",");
  } else {
    productIdsInput.value = "";
  }

  // 如果有圖片,設定 fileList
  if (row.imageUrl) {
    // 將相對路徑轉為完整 URL
    const baseUrl =
      import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const fullImageUrl = row.imageUrl.startsWith("http")
      ? row.imageUrl
      : `${baseUrl}${row.imageUrl}`;

    imageFileList.value = [
      {
        id: "current",
        name: "當前圖片",
        status: "finished",
        url: fullImageUrl,
      },
    ];
  } else {
    imageFileList.value = [];
  }

  showModal.value = true;
};

// 儲存
const handleSubmit = async () => {
  submitting.value = true;
  try {
    if (!formData.value.imageUrl) {
      message.warning("請上傳圖片");
      submitting.value = false;
      return;
    }

    const payload: any = {
      type: formData.value.type,
      title: formData.value.title || undefined,
      subtitle: formData.value.subtitle || undefined,
      imageUrl: formData.value.imageUrl,
      linkUrl: formData.value.linkUrl || undefined,
      sortOrder: formData.value.sortOrder,
      isActive: formData.value.isActive,
      startTime: formData.value.startTime,
      endTime: formData.value.endTime,
    };

    // 處理商品 ID
    if (formData.value.type === "PRODUCT_RECOMMEND" && productIdsInput.value) {
      payload.productIds = productIdsInput.value
        .split(",")
        .map((id) => parseInt(id.trim()))
        .filter((id) => !isNaN(id));
    }

    if (isEdit.value && formData.value.blockId) {
      await homeBlockApi.updateBlock(formData.value.blockId, payload);
      message.success("更新成功");
    } else {
      await homeBlockApi.createBlock(payload);
      message.success("新增成功");
    }

    showModal.value = false;
    loadBlocks();
  } catch (error: any) {
    message.error(error.response?.data?.message || "操作失敗");
    console.error(error);
  } finally {
    submitting.value = false;
  }
};

// 刪除
const handleDelete = (id: number) => {
  dialog.warning({
    title: "確認刪除",
    content: "確定要刪除此區塊嗎？",
    positiveText: "確定",
    negativeText: "取消",
    onPositiveClick: async () => {
      try {
        await homeBlockApi.deleteBlock(id);
        message.success("刪除成功");
        loadBlocks();
      } catch (error) {
        message.error("刪除失敗");
        console.error(error);
      }
    },
  });
};

onMounted(() => {
  loadBlocks();
});
</script>

<style scoped lang="scss">
.home-blocks-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }

  :deep(.n-data-table) {
    border-radius: var(--radius-md) !important;
  }
}
</style>
