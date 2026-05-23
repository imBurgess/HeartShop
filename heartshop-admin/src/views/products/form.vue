<template>
  <div class="product-form-page">
    <n-card :title="isEdit ? '編輯商品' : '新增商品'" :bordered="false">
      <template #header-extra>
        <n-button @click="handleBack">返回列表</n-button>
      </template>

      <n-spin :show="loading">
        <n-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-placement="left"
          label-width="120"
          require-mark-placement="left"
        >
          <n-grid :cols="24" :x-gap="24">
            <!-- 基本資訊 -->
            <n-form-item-gi
              :span="24"
              label="商品編號"
              path="code"
              v-if="isEdit"
            >
              <n-input
                v-model:value="formData.code"
                placeholder="請輸入商品編號"
                disabled
              />
            </n-form-item-gi>
            <n-form-item-gi :span="24" label="商品編號" v-else>
              <span style="color: #999; font-size: 14px"
                >（系統將自動生成商品編號）</span
              >
            </n-form-item-gi>

            <n-form-item-gi :span="12" label="中文名稱" path="name">
              <n-input
                v-model:value="formData.name"
                placeholder="請輸入中文商品名稱"
              />
            </n-form-item-gi>

            <n-form-item-gi :span="12" label="英文名稱" path="nameEn">
              <n-input
                v-model:value="formData.nameEn"
                placeholder="請輸入英文商品名稱"
              />
            </n-form-item-gi>

            <!-- 分類 -->
            <n-form-item-gi :span="12" label="商品分類" path="categoryId">
              <n-select
                v-model:value="formData.categoryId"
                placeholder="請選擇分類"
                :options="categoryOptions"
              />
            </n-form-item-gi>

            <!-- 價格 -->
            <n-form-item-gi :span="6" label="售價" path="price">
              <n-input-number
                v-model:value="formData.price"
                placeholder="0"
                :min="0"
                :precision="0"
                style="width: 100%"
              >
                <template #prefix>NT$</template>
              </n-input-number>
            </n-form-item-gi>

            <n-form-item-gi :span="6" label="折扣價" path="discountPrice">
              <n-input-number
                v-model:value="formData.discountPrice"
                placeholder="選填"
                :min="0"
                :precision="0"
                style="width: 100%"
              >
                <template #prefix>NT$</template>
              </n-input-number>
            </n-form-item-gi>

            <!-- 商品描述 -->
            <n-form-item-gi :span="24" label="商品描述" path="description">
              <n-input
                v-model:value="formData.description"
                type="textarea"
                placeholder="請輸入商品描述"
                :rows="4"
              />
            </n-form-item-gi>

            <!-- 尺寸資訊 -->
            <n-form-item-gi :span="24" label="尺寸資訊" path="sizeInfo">
              <n-input
                v-model:value="formData.sizeInfo"
                type="textarea"
                placeholder="請輸入尺寸資訊，例如：S / M / L / XL"
                :rows="2"
              />
            </n-form-item-gi>

            <!-- 標籤 -->
            <n-form-item-gi :span="24" label="標籤" path="tags">
              <n-input
                v-model:value="formData.tags"
                placeholder="請輸入標籤，多個標籤用逗號分隔，例如：熱銷,限量,新品"
              />
            </n-form-item-gi>

            <!-- 庫存數量 -->
            <n-form-item-gi :span="6" :label="isEdit ? '庫存數量' : '初始數量'" path="stock">
              <n-input-number
                v-model:value="formData.stock"
                placeholder="0"
                :min="0"
                :precision="0"
                style="width: 100%"
              >
                <template #suffix>件</template>
              </n-input-number>
            </n-form-item-gi>

            <!-- 排序 -->
            <n-form-item-gi :span="12" label="排序順序" path="sortOrder">
              <n-input-number
                v-model:value="formData.sortOrder"
                placeholder="數字越小越前面"
                :min="0"
                style="width: 100%"
              />
            </n-form-item-gi>

            <!-- 狀態設定 -->
            <n-form-item-gi :span="24" label="商品狀態">
              <n-space>
                <n-checkbox v-model:checked="formData.isActive"
                  >上架</n-checkbox
                >
                <n-checkbox v-model:checked="formData.isNew">新品</n-checkbox>
                <n-checkbox v-model:checked="formData.isSoldOut"
                  >售完</n-checkbox
                >
              </n-space>
            </n-form-item-gi>

            <!-- 圖片上傳（暫時保留，等後端支援） -->
            <!-- 圖片上傳 -->
            <n-form-item-gi :span="24" label="商品圖片">
              <n-upload
                :action="uploadUrl"
                v-model:file-list="fileList"
                list-type="image-card"
                name="file"
                :headers="uploadHeaders"
                :max="5"
                @finish="handleUploadFinish"
                @remove="handleRemove"
                @error="handleUploadError"
              >
                點擊上傳
              </n-upload>
            </n-form-item-gi>

            <!-- 操作按鈕 -->
            <n-form-item-gi :span="24">
              <n-space>
                <n-button
                  type="primary"
                  @click="handleSubmit"
                  :loading="submitting"
                >
                  {{ isEdit ? "儲存" : "新增" }}
                </n-button>
                <n-button @click="handleBack">取消</n-button>
              </n-space>
            </n-form-item-gi>
          </n-grid>
        </n-form>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useProductStore } from "@/stores/product";
import { getActiveCategories } from "@/api/category";
import type { Category } from "@/api/category";
import type { FormInst, FormRules, UploadFileInfo } from "naive-ui";
import { useMessage } from "naive-ui";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const route = useRoute();
const productStore = useProductStore();
const message = useMessage();
const authStore = useAuthStore();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);
const submitting = ref(false);

// 上傳相關
const fileList = ref<UploadFileInfo[]>([]);
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${baseUrl}/api/upload/image`;
});
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${authStore.token}`,
}));

// 是否為編輯模式
const isEdit = computed(() => !!route.params.id);
const productId = computed(() =>
  route.params.id ? Number(route.params.id) : null
);

// 表單資料
const formData = reactive({
  code: "",
  name: "",
  nameEn: "",
  categoryId: undefined as number | undefined,
  price: 0,
  discountPrice: undefined as number | undefined,
  description: "",
  sizeInfo: "",
  tags: "",
  sortOrder: 0,
  isActive: true,
  isNew: false,
  isSoldOut: false,
  images: [] as string[],
  stock: 0,
});

// 分類選項
const categoryOptions = ref<{ label: string; value: number }[]>([]);

// 表單驗證規則
const rules: FormRules = {
  // code: [
  //   { required: true, message: "請輸入商品編號", trigger: "blur" },
  //   { min: 2, max: 50, message: "長度在 2 到 50 個字元", trigger: "blur" },
  // ],
  name: [
    { required: true, message: "請輸入中文名稱", trigger: "blur" },
    { min: 2, max: 100, message: "長度在 2 到 100 個字元", trigger: "blur" },
  ],
  categoryId: [
    {
      required: true,
      type: "number",
      message: "請選擇分類",
      trigger: "change",
    },
  ],
  price: [
    { required: true, type: "number", message: "請輸入售價", trigger: "blur" },
    { type: "number", min: 0, message: "售價必須大於等於 0", trigger: "blur" },
  ],
};

// 上傳事件處理
const handleUploadFinish = ({
  file,
  event,
}: {
  file: UploadFileInfo;
  event?: ProgressEvent;
}) => {
  try {
    const res = JSON.parse((event?.target as XMLHttpRequest).response);
    // 修正：後端 ApiResponse 的 success code 是 "0000"，不是 "200"
    if (res.code === "0000") {
      file.url = res.data.url;
      file.status = "finished";
      message.success("上傳成功");
      updateFormImages();
    } else {
      message.error(res.message || "上傳失敗");
      file.status = "error";
    }
  } catch (e) {
    message.error("上傳回應解析失敗");
    file.status = "error";
  }
  return file;
};

const handleRemove = () => {
  // 稍等 fileList 更新
  setTimeout(() => updateFormImages(), 100);
};

const handleUploadError = () => {
  message.error("上傳發生錯誤");
};

// 同步 fileList 到 formData.images
const updateFormImages = () => {
  // 取出所有 status 為 finished 且有 url 的檔案
  // 注意: 這裡我們只存相對路徑或絕對路徑，看後端需求。
  // 後端回傳的是相對路徑 /uploads/...
  // 前端顯示時如果是相對路徑，NUpload image-card 模式會自己處理嗎？通常需要完整網址。
  // 但我們在 handleFinish 把 url 設為了 /uploads/...
  // 如果 NUpload 顯示不出圖片，可能需要補上 base URL。

  // 為了儲存，我們只需要 URL 字串
  formData.images = fileList.value
    .filter((f) => f.status === "finished" && f.url)
    .map((f) => {
      // 如果 url 包含 domain，移除它 (如果後端只存相對路徑)
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
      if (f.url && f.url.startsWith(baseUrl)) {
        return f.url.replace(baseUrl, "");
      }
      return f.url as string;
    });
};

// 載入分類
const loadCategories = async () => {
  try {
    const response = await getActiveCategories();
    const categories = response.data || [];
    categoryOptions.value = categories.map((c: Category) => ({
      label: c.nameZh,
      value: c.categoryId,
    }));
  } catch (error) {
    console.error("載入分類失敗:", error);
  }
};

// 載入商品資料（編輯模式）
const loadProduct = async () => {
  if (!productId.value) return;

  loading.value = true;
  try {
    const product = await productStore.fetchProductById(productId.value);

    // 填充表單
    formData.code = product.code;
    formData.name = product.name;
    formData.nameEn = product.nameEn;
    formData.categoryId = product.categoryId;
    formData.price = product.price;
    formData.discountPrice = product.discountPrice || undefined;
    formData.description = product.description || "";
    formData.sizeInfo = product.sizeInfo || "";
    formData.tags = Array.isArray(product.tags)
      ? product.tags.join(",")
      : product.tags || "";
    formData.sortOrder = product.sortOrder || 0;
    formData.isActive = product.isActive;
    formData.isNew = product.isNew;
    formData.isSoldOut = product.isSoldOut;
    formData.images = product.images || [];
    formData.stock = product.stockQuantity ?? 0;

    // 還原圖片列表
    if (product.images && product.images.length > 0) {
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
      fileList.value = product.images.map((url, index) => ({
        id: String(index),
        name: `image-${index}`,
        status: "finished",
        url: url.startsWith("http") ? url : `${baseUrl}${url}`,
      }));
    }
  } catch (error: any) {
    message.error(error.message || "載入商品失敗");
    handleBack();
  } finally {
    loading.value = false;
  }
};

// 送出表單
const handleSubmit = async () => {
  // 驗證表單
  await formRef.value?.validate();

  // 確保必填欄位有值
  if (!formData.categoryId) {
    message.error("請選擇分類");
    return;
  }

  // 確保圖片同步
  updateFormImages();

  submitting.value = true;
  try {
    // 準備提交資料
    // tags 字串轉陣列
    const tagsArray = formData.tags
      ? formData.tags
          .split(",")
          .map((t) => t.trim())
          .filter((t) => t)
      : [];

    const baseData = {
      ...formData,
      categoryId: formData.categoryId!,
      tags: tagsArray as any,
    };

    if (isEdit.value && productId.value) {
      await productStore.updateProduct(productId.value, baseData);
      message.success("更新成功");
    } else {
      const submitData = { ...baseData, initialStock: formData.stock };
      await productStore.createProduct(submitData);
      message.success("新增成功");
    }
    handleBack();
  } catch (error: any) {
    message.error(error.message || `${isEdit.value ? "更新" : "新增"}失敗`);
  } finally {
    submitting.value = false;
  }
};

// 返回列表
const handleBack = () => {
  router.push("/products");
};

onMounted(() => {
  loadCategories();
  if (isEdit.value) {
    loadProduct();
  }
});
</script>

<style scoped lang="scss">
.product-form-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;

    .n-card-header {
      padding: var(--spacing-md) var(--spacing-md) var(--spacing) !important;
      border-bottom: 1px solid var(--color-gray-200) !important;

      .n-card__header-main {
        font-size: 1.25rem;
        font-weight: var(--font-weight-bold);
        color: var(--color-gray-900);
      }
    }

    .n-card__content {
      padding: var(--spacing-md) !important;
    }
  }

  :deep(.n-form) {
    .n-form-item {
      margin-bottom: var(--spacing-md) !important;

      .n-form-item-label {
        font-weight: var(--font-weight-medium) !important;
        color: var(--color-gray-700) !important;
      }
    }
  }

  :deep(.n-input),
  :deep(.n-input-number) {
    .n-input__border,
    .n-input__state-border {
      border-radius: var(--radius) !important;
    }

    &:hover .n-input__state-border {
      border-color: var(--color-primary) !important;
    }

    &.n-input--focus .n-input__state-border {
      box-shadow: 0 0 0 3px var(--color-primary-bg) !important;
    }
  }

  :deep(.n-select) {
    .n-base-selection {
      border-radius: var(--radius) !important;

      &:hover {
        border-color: var(--color-primary) !important;
      }
    }

    &.n-select--focused .n-base-selection {
      box-shadow: 0 0 0 3px var(--color-primary-bg) !important;
    }
  }

  :deep(.n-checkbox) {
    .n-checkbox__label {
      font-weight: var(--font-weight-medium) !important;
      color: var(--color-gray-700) !important;
    }
  }

  :deep(.n-button) {
    border-radius: var(--radius) !important;
    font-weight: var(--font-weight-medium) !important;
    padding: 0 var(--spacing-md) !important;
    transition: all var(--transition-base) !important;

    &.n-button--primary-type {
      background: linear-gradient(
        135deg,
        var(--color-primary) 0%,
        var(--color-primary-dark) 100%
      ) !important;
      border: none !important;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: var(--shadow-lg) !important;
      }
    }

    &.n-button--default-type {
      &:hover {
        border-color: var(--color-primary) !important;
        color: var(--color-primary) !important;
      }
    }
  }

  :deep(.n-alert) {
    border-radius: var(--radius) !important;
  }
}
</style>
