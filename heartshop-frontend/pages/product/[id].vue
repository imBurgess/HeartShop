<!-- pages/product/[id].vue -->
<template>
  <!-- 載入中 -->
  <main class="pdPage" v-if="isLoading">
    <p style="text-align: center; padding: 60px 0">載入中...</p>
  </main>

  <!-- 錯誤訊息 -->
  <main class="pdPage" v-else-if="errorMessage">
    <p style="text-align: center; padding: 60px 0; color: #353535">
      {{ errorMessage }}
    </p>
  </main>

  <!-- 商品內容 -->
  <main class="pdPage" v-else-if="product">
    <!-- ===== 第一段：左圖 + 右側資訊 ===== -->
    <section class="pdMain">
      <!-- 左：圖片區 -->
      <div class="pdGallery" :class="{ 'pdGallery--single': productImages.length <= 1 }">
        <!-- 小縮圖：直排 -->
        <div class="pdThumbList" v-if="productImages.length > 1">
          <button
            v-for="(img, index) in productImages"
            :key="img"
            class="pdThumb"
            :class="{ active: index === mainImageIndex }"
            @click="mainImageIndex = index"
          >
            <img :src="img" :alt="product.name" />
          </button>
        </div>

        <!-- 大圖 -->
        <div class="pdMainPhoto">
          <img :src="currentImage" :alt="product.name" />
        </div>
      </div>

      <!-- 右：商品資訊 -->
      <div class="pdInfo">
        <p class="pdCode">{{ product.code }}</p>
        <h1 class="pdName">{{ product.name }}</h1>
        <p class="pdEnName" v-if="product.nameEn">{{ product.nameEn }}</p>

        <p class="pdPrice">NT $ {{ displayPrice.toLocaleString() }}</p>

        <!-- （顏色區塊已移除） -->

        <!-- SIZE / QTY -->
        <div class="pdSelectRow">
          <label class="pdSelect" v-if="hasRealSizes">
            <span>SIZE</span>
            <select v-model="selectedSize">
              <option value="" disabled>請選擇尺寸</option>
              <option v-for="size in availableSizes" :key="size" :value="size">
                {{ size }}
              </option>
            </select>
          </label>

          <label class="pdSelect">
            <span>QTY</span>
            <select v-model.number="selectedQty" :disabled="product.isSoldOut">
              <option v-for="n in 10" :key="n" :value="n">
                {{ n }}
              </option>
            </select>
          </label>
        </div>

        <!-- 加入購物車 -->
        <button
          class="pdAddBtn"
          :class="{ 'pdAddBtn--soldout': product.isSoldOut, 'pdAddBtn--loading': cartStore.loading }"
          :disabled="product.isSoldOut || cartStore.loading"
          @click="handleAddToBag"
        >
          <span v-if="product.isSoldOut">SOLD OUT</span>
          <span v-else-if="cartStore.loading">加入中...</span>
          <span v-else>ADD TO BAG</span>
        </button>

        <div class="pdSubLinks">
          <button
            class="wishBtn"
            :class="{ 'wishBtn--active': isWishlisted, 'wishBtn--loading': wishLoading }"
            type="button"
            :title="isWishlisted ? '取消收藏' : '加入收藏'"
            :disabled="wishLoading"
            @click="handleToggleWish"
          >
            <svg class="heartIcon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path v-if="isWishlisted"
                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5
                   2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09
                   C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5
                   c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              <path v-else
                d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3
                   4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35
                   l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3z
                   m-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5
                   4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87
                   C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5
                   0 2.89-3.14 5.74-7.9 10.05z"/>
            </svg>
            <span>{{ isWishlisted ? '已收藏' : '收藏' }}</span>
          </button>
          <button class="linkBtn" type="button" @click="openQaModal">Q&amp;A +</button>
        </div>
      </div>
    </section>

    <!-- Q&A Modal -->
    <n-modal
      v-model:show="qaModalVisible"
      preset="card"
      title="商品問答 Q&A"
      class="qa-modal"
      :style="{ width: '560px', maxWidth: '94vw' }"
      :segmented="{ content: true, footer: true }"
    >
      <!-- 既有問答列表 -->
      <div class="qa-list">
        <p v-if="qaLoading" class="qa-empty">載入中...</p>
        <template v-else-if="qaItems.length > 0">
          <div v-for="item in qaItems" :key="item.qaId" class="qa-item">
            <div class="qa-q">
              <span class="qa-badge qa-badge--q">Q</span>
              <div class="qa-content">
                <p class="qa-text">{{ item.question }}</p>
                <span class="qa-meta">{{ item.memberName || '匿名' }} · {{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
            <div class="qa-a">
              <span class="qa-badge qa-badge--a">A</span>
              <div class="qa-content">
                <p v-if="item.answer" class="qa-text">{{ item.answer }}</p>
                <p v-else class="qa-text qa-text--pending">等待客服回覆...</p>
              </div>
            </div>
          </div>
        </template>
        <p v-else class="qa-empty">目前尚無問答，歡迎提問！</p>
      </div>

      <!-- 提問區 -->
      <template #footer>
        <div v-if="isLoggedIn" class="qa-form">
          <p class="qa-form-label">提問</p>
          <n-input
            v-model:value="qaQuestion"
            type="textarea"
            placeholder="請輸入您的問題，客服人員將盡快回覆"
            :rows="3"
            :maxlength="500"
            show-count
          />
          <div class="qa-form-actions">
            <n-button
              type="primary"
              :loading="qaSubmitting"
              :disabled="!qaQuestion.trim()"
              @click="submitQuestion"
            >
              送出提問
            </n-button>
          </div>
        </div>
        <div v-else class="qa-login-hint">
          <span>請先</span>
          <n-button text type="primary" @click="goToLogin">登入</n-button>
          <span>才能提問</span>
        </div>
      </template>
    </n-modal>

    <!-- ===== 第二段：下方 Tabs（Detail / Size Info） ===== -->
    <section class="pdTabs">
      <div class="pdTabHeader">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="pdTabBtn"
          :class="{ active: currentTab === tab.key }"
          @click="currentTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="pdTabBody">
        <div v-if="currentTab === 'detail'">
          <p v-if="product.description">{{ product.description }}</p>
          <p v-else style="color: #353535">暫無商品描述</p>
        </div>
        <div v-else-if="currentTab === 'size'">
          <p v-if="product.sizeInfo">{{ product.sizeInfo }}</p>
          <p v-else style="color: #353535">暫無尺寸資訊</p>
        </div>
      </div>
    </section>

    <!-- ===== 第三段：YOU MAY ALSO LIKE ===== -->
    <section class="pdSection" v-if="alsoLike.length > 0">
      <h2 class="pdSectionTitle">YOU MAY ALSO LIKE</h2>

      <div class="pdRecommendList">
        <article
          v-for="item in alsoLike"
          :key="item.productId"
          class="pdCard"
          @click="goProduct(item.productId)"
        >
          <div class="thumb">
            <img :src="getFullImageUrl(item.imageUrl)" :alt="item.name" />
          </div>
          <p class="name">{{ item.name }}</p>
          <p class="price">
            NT $ {{ (item.discountPrice || item.price || 0).toLocaleString() }}
          </p>
        </article>
      </div>
    </section>
  </main>

  <!-- 無法載入商品（fallback） -->
  <main v-else class="pdPage">
    <p style="text-align: center; padding: 60px 0">找不到此商品。</p>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { productService, type Product } from "@/services/product";
import { useCartStore } from "@/stores/cart";
import { wishlistService } from "@/services/wishlist";
import { qaService, type QaItem } from "@/services/qa";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const cartStore = useCartStore();

// 取得網址中的 id：/product/1
const id = computed(() => Number(route.params.id));

// 商品資料
const product = ref<Product | null>(null);
const isLoading = ref(true);
const errorMessage = ref("");

// 推薦商品（YOU MAY ALSO LIKE）
const alsoLike = ref<Product[]>([]);

// 圖片 URL 轉換函數
const getFullImageUrl = (imageUrl?: string): string => {
  if (!imageUrl) return "";
  // 如果已經是完整 URL,直接返回
  if (imageUrl.startsWith("http")) return imageUrl;
  // 否則加上後端 baseURL
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${baseUrl}${imageUrl}`;
};

// 主要大圖 index
const mainImageIndex = ref(0);
const currentImage = computed(() => {
  const p = product.value;
  if (!p) return "";

  // 優先使用 images 陣列
  if (p.images && p.images.length > 0) {
    return getFullImageUrl(p.images[mainImageIndex.value] || p.images[0]);
  }

  // 若無 images，使用 imageUrl
  return getFullImageUrl(p.imageUrl);
});

// 圖片列表（用於縮圖顯示）
const productImages = computed(() => {
  const p = product.value;
  if (!p) return [];

  // 優先使用 images 陣列
  if (p.images && p.images.length > 0) {
    return p.images.map((img) => getFullImageUrl(img));
  }

  // 若無 images，使用 imageUrl
  if (p.imageUrl) {
    return [getFullImageUrl(p.imageUrl)];
  }

  return [];
});

// 是否有真實尺寸選項（決定是否顯示 SIZE 下拉）
const hasRealSizes = computed(() => availableSizes.value.length > 0);

// 尺寸選項（從 sizeInfo 解析）
const availableSizes = computed(() => {
  const p = product.value;
  if (!p || !p.sizeInfo) return [];
  return p.sizeInfo
    .split(/[,/]/)
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
});

// 尺寸 / 數量
const selectedSize = ref<string>("");

// 只有一個尺寸時自動選取；無尺寸時預設 Free Size
watch(availableSizes, (sizes) => {
  if (sizes.length === 1) {
    selectedSize.value = sizes[0];
  } else if (sizes.length === 0) {
    selectedSize.value = "Free Size";
  }
}, { immediate: true });
const selectedQty = ref(1);

// 顯示價格（優先顯示折扣價）
const displayPrice = computed(() => {
  const p = product.value;
  if (!p) return 0;

  if (p.discountPrice && p.discountPrice > 0) {
    return p.discountPrice;
  }

  return p.price || 0;
});

// Tabs（DETAIL / SIZE INFO）
const tabs = [
  { key: "detail", label: "DETAIL" },
  { key: "size", label: "SIZE INFO" },
] as const;

type TabKey = (typeof tabs)[number]["key"];

const currentTab = ref<TabKey>("detail");

// 載入商品資料
const loadProduct = async () => {
  try {
    isLoading.value = true;
    errorMessage.value = "";

    const productData = await productService.getProductById(id.value);

    // apiFetch 已經提取 response.data，直接使用即可
    if (productData) {
      product.value = productData;

      // 載入推薦商品（同分類）
      await loadRecommendedProducts();
    } else {
      errorMessage.value = "找不到此商品";
    }
  } catch (error: any) {
    console.error("載入商品失敗:", error);
    errorMessage.value = error?.message || "載入商品失敗，請稍後再試";
  } finally {
    isLoading.value = false;
  }
};

// 載入推薦商品（同分類的其他商品）
const loadRecommendedProducts = async () => {
  try {
    const p = product.value;
    if (!p) return;

    const productsData = await productService.getProducts({
      categoryId: p.categoryId,
      isActive: true,
      pageSize: 10,
    });

    // productsData 格式：{ items: [...], total: ... }
    if (productsData && productsData.items) {
      // 過濾掉當前商品，隨機取 3 個
      const filtered = productsData.items.filter(
        (item: Product) => item.productId !== p.productId
      );

      const shuffled = filtered.sort(() => Math.random() - 0.5);
      alsoLike.value = shuffled.slice(0, 3);
    }
  } catch (error) {
    console.error("載入推薦商品失敗:", error);
    // 不顯示錯誤，推薦商品載入失敗不影響主頁面
  }
};

// 加入購物車
const handleAddToBag = async () => {
  const p = product.value;
  if (!p || cartStore.loading) return;

  if (p.isSoldOut) {
    message.warning("此商品已售完");
    return;
  }

  if (hasRealSizes.value && !selectedSize.value) {
    message.warning("請先選擇尺寸");
    return;
  }

  const token = useCookie("token").value;
  if (!token) {
    message.warning("請先登入");
    router.push("/member");
    return;
  }

  const sizeToUse = selectedSize.value || "Free Size";
  const success = await cartStore.addToCart(p.productId, sizeToUse, selectedQty.value);

  if (success) {
    const sizeLabel = sizeToUse !== "Free Size" ? ` (${sizeToUse})` : "";
    message.success(`已加入購物車：${p.name}${sizeLabel} x ${selectedQty.value}`);
  } else {
    message.error("加入購物車失敗，請稍後再試");
  }
};

// 跳到其他商品
const goProduct = (pid: number) => {
  router.push(`/product/${pid}`);
  mainImageIndex.value = 0;
  selectedSize.value = "";
  selectedQty.value = 1;
  isWishlisted.value = false;
  loadProduct();
  loadWishlistStatus();
};

/* ── 收藏功能 ── */
const isWishlisted = ref(false);
const wishLoading = ref(false);

const loadWishlistStatus = async () => {
  const token = useCookie("token").value;
  if (!token || !id.value) return;
  try {
    isWishlisted.value = await wishlistService.checkStatus(id.value);
  } catch {
    // 未登入或 API 失敗時靜默忽略
  }
};

const handleToggleWish = async () => {
  const token = useCookie("token").value;
  if (!token) {
    message.warning("請先登入");
    router.push("/member");
    return;
  }
  if (wishLoading.value || !id.value) return;
  wishLoading.value = true;
  try {
    const nowWishlisted = await wishlistService.toggle(id.value);
    isWishlisted.value = nowWishlisted;
    message.success(nowWishlisted ? "已加入收藏 ♥" : "已取消收藏");
  } catch (err: any) {
    message.error(err.message || "操作失敗，請稍後再試");
  } finally {
    wishLoading.value = false;
  }
};

/* ── Q&A 功能 ── */
const qaModalVisible = ref(false);
const qaItems = ref<QaItem[]>([]);
const qaLoading = ref(false);
const qaQuestion = ref("");
const qaSubmitting = ref(false);
const isLoggedIn = computed(() => !!useCookie("token").value);

const formatDate = (dateStr: string) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

const openQaModal = async () => {
  qaModalVisible.value = true;
  if (qaItems.value.length === 0) {
    await loadQa();
  }
};

const loadQa = async () => {
  qaLoading.value = true;
  try {
    qaItems.value = await qaService.getQa(id.value);
  } catch {
    // silent fail
  } finally {
    qaLoading.value = false;
  }
};

const submitQuestion = async () => {
  if (!qaQuestion.value.trim() || qaSubmitting.value) return;
  qaSubmitting.value = true;
  try {
    const newQa = await qaService.addQuestion(id.value, qaQuestion.value.trim());
    qaItems.value.unshift(newQa);
    qaQuestion.value = "";
    message.success("提問已送出，客服將盡快回覆！");
  } catch (err: any) {
    message.error(err.message || "提問失敗，請稍後再試");
  } finally {
    qaSubmitting.value = false;
  }
};

const goToLogin = () => {
  qaModalVisible.value = false;
  router.push("/member");
};

// 組件掛載時載入商品
onMounted(() => {
  loadProduct();
  loadWishlistStatus();
});
</script>

<style scoped lang="scss">
.pdPage {
  width: min(1100px, 94%);
  margin: 32px auto 80px;
}

/* ===== 第一段：主區塊 ===== */
.pdMain {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

/* 左：圖片 */
.pdGallery {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;

  &--single {
    grid-template-columns: 1fr;
  }
}

.pdThumbList {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pdThumb {
  padding: 0;
  border: 1px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  width: 64px;
  height: 64px;

  &.active {
    border-color: #000;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
  }
}

.pdMainPhoto {
  border: 1px solid #ddd;
  background: #f7f7f7;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: transform 0.35s ease;
  }

  &:hover img {
    transform: scale(1.08);
  }
}

/* 右：商品資訊 */
.pdInfo {
  font-size: 14px;
}

.pdCode {
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}

.pdName {
  font-size: 18px;
  margin: 0 0 4px;
}
.pdEnName {
  font-size: 15px;
  margin: 0 0 12px;
}

.pdPrice {
  color: #353535;
  font-size: 18px;
  margin-bottom: 20px;
}

/* 尺寸 / 數量 */
.pdSelectRow {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.pdSelect {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  letter-spacing: 0.08em;

  select {
    height: 36px;
    border: 1px solid #ccc;
    padding: 0 8px;
    font-size: 13px;
  }
}

/* ADD TO BAG */
.pdAddBtn {
  width: 100%;
  height: 40px;
  margin-top: 4px;
  border: none;
  background: #000;
  color: #fff;
  letter-spacing: 0.12em;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;

  &--soldout {
    background: #aaa;
    cursor: not-allowed;
  }

  &--loading {
    background: #555;
    cursor: wait;
  }

  &:disabled {
    cursor: not-allowed;
  }
}

/* WISH / Q&A / icon */
.pdSubLinks {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 16px;

  .linkBtn {
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    font-size: 12px;
    letter-spacing: 0.12em;
  }
}

/* 收藏愛心按鈕 */
.wishBtn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #888;
  transition: color 0.2s;

  .heartIcon {
    width: 17px;
    height: 17px;
    fill: #ccc;
    transition: fill 0.25s, transform 0.2s;
  }

  &--active {
    color: #e05a6a;
    .heartIcon {
      fill: #e05a6a;
    }
  }

  &--loading {
    opacity: 0.6;
    cursor: wait;
  }

  &:hover:not(:disabled) {
    color: #e05a6a;
    .heartIcon {
      fill: #e05a6a;
      transform: scale(1.15);
    }
  }
}

/* ===== 第二段：Tabs ===== */
.pdTabs {
  border-top: 1px solid #ddd;
  padding-top: 20px;
  margin-bottom: 40px;
  font-size: 13px;
}

.pdTabHeader {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.pdTabBtn {
  background: none;
  border: none;
  padding: 0 0 4px;
  cursor: pointer;
  letter-spacing: 0.12em;
  font-size: 12px;

  &.active {
    border-bottom: 1px solid #000;
  }
}

.pdTabBody {
  min-height: 60px;
}

/* ===== YOU MAY ALSO LIKE & RECENTLY VIEWED ===== */
.pdSection {
  margin-bottom: 40px;
}

.pdSectionTitle {
  font-size: 13px;
  letter-spacing: 0.14em;
  margin-bottom: 16px;
}

.pdRecommendList {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.pdRecentList {
  display: flex;
  gap: 18px;
}

.pdCard {
  cursor: pointer;
  font-size: 12px;

  .thumb {
    border: 1px solid #ddd;
    background: #f7f7f7;
    margin-bottom: 6px;
    aspect-ratio: 1 / 1;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: contain;
      transition: transform 0.35s ease;
    }
  }

  &:hover .thumb img {
    transform: scale(1.1);
  }

  .name {
    margin: 0 0 4px;
  }

  .price {
    margin: 0;
  }

  &.small .thumb img {
    height: 140px;
    object-fit: cover;
  }
}

/* ===== Q&A Modal ===== */
.qa-modal {
  font-size: 13px;
}

.qa-list {
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-right: 4px;
}

.qa-empty {
  color: #999;
  text-align: center;
  padding: 24px 0;
  margin: 0;
}

.qa-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.qa-q,
.qa-a {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.qa-badge {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;

  &--q {
    background: #353535;
    color: #fff;
  }

  &--a {
    background: #e8f4f8;
    color: #555;
  }
}

.qa-content {
  flex: 1;
}

.qa-text {
  margin: 0 0 2px;
  line-height: 1.5;
  color: #353535;

  &--pending {
    color: #aaa;
    font-style: italic;
  }
}

.qa-meta {
  font-size: 11px;
  color: #bbb;
}

.qa-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.qa-form-label {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

.qa-form-actions {
  display: flex;
  justify-content: flex-end;
}

.qa-login-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #888;
  justify-content: center;
  padding: 4px 0;
}

/* RWD */
@media (max-width: 900px) {
  .pdMain {
    grid-template-columns: 1fr;
  }

  .pdGallery {
    grid-template-columns: 64px 1fr;
  }

  .pdRecommendList {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
