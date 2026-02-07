<!-- pages/product/[id].vue -->
<template>
  <!-- 載入中 -->
  <main class="pdPage" v-if="isLoading">
    <p style="text-align: center; padding: 60px 0">載入中...</p>
  </main>

  <!-- 錯誤訊息 -->
  <main class="pdPage" v-else-if="errorMessage">
    <p style="text-align: center; padding: 60px 0; color: #d60000">
      {{ errorMessage }}
    </p>
  </main>

  <!-- 商品內容 -->
  <main class="pdPage" v-else-if="product">
    <!-- ===== 第一段：左圖 + 右側資訊 ===== -->
    <section class="pdMain">
      <!-- 左：圖片區 -->
      <div class="pdGallery">
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
          <label class="pdSelect">
            <span>SIZE</span>
            <select
              v-model="selectedSize"
              :disabled="availableSizes.length === 0"
            >
              <option value="" disabled>請選擇尺寸</option>
              <option v-for="size in availableSizes" :key="size" :value="size">
                {{ size }}
              </option>
            </select>
          </label>

          <label class="pdSelect">
            <span>QTY</span>
            <select v-model.number="selectedQty">
              <option v-for="n in 10" :key="n" :value="n">
                {{ n }}
              </option>
            </select>
          </label>
        </div>

        <!-- 加入購物車 -->
        <button class="pdAddBtn" @click="handleAddToBag">ADD TO BAG</button>

        <div class="pdSubLinks">
          <button class="linkBtn" type="button">WISH LIST</button>
          <button class="linkBtn" type="button">Q&amp;A +</button>
        </div>
      </div>
    </section>

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
          <p v-else style="color: #999">暫無商品描述</p>
        </div>
        <div v-else-if="currentTab === 'size'">
          <p v-if="product.sizeInfo">{{ product.sizeInfo }}</p>
          <p v-else style="color: #999">暫無尺寸資訊</p>
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
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { productService, type Product } from "@/services/product";

const route = useRoute();
const router = useRouter();

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
  const baseUrl = "http://localhost:8080";
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

// 尺寸選項（從 sizeInfo 解析）
const availableSizes = computed(() => {
  const p = product.value;
  if (!p || !p.sizeInfo) return [];

  // sizeInfo 可能格式：
  // 1. "S,M,L"
  // 2. "S / M / L"
  // 3. 其他格式，這裡簡單用逗號或斜線分割
  return p.sizeInfo
    .split(/[,/]/)
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
});

// 尺寸 / 數量
const selectedSize = ref<string | "">("");
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
const handleAddToBag = () => {
  const p = product.value;
  if (!p) return;

  if (!selectedSize.value) {
    window.alert("請先選擇尺寸");
    return;
  }

  console.log("加入購物車：", {
    id: p.productId,
    name: p.name,
    size: selectedSize.value,
    qty: selectedQty.value,
    price: displayPrice.value,
  });

  window.alert(
    `已加入購物車：${p.name} (${selectedSize.value}) x ${selectedQty.value}`
  );

  // 之後可以在這裡呼叫後端 API 或丟到 Pinia
};

// 跳到其他商品
const goProduct = (pid: number) => {
  router.push(`/product/${pid}`);
  // 切換商品後重新載入資料
  mainImageIndex.value = 0;
  selectedSize.value = "";
  selectedQty.value = 1;
  loadProduct();
};

// 組件掛載時載入商品
onMounted(() => {
  loadProduct();
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
    object-fit: cover;
    display: block;
  }
}

.pdMainPhoto {
  border: 1px solid #ddd;
  background: #f7f7f7;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    max-height: 520px;
    object-fit: contain;
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
  color: #d60000;
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

  .iconBtn {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    border: 1px solid #000;
    background: none;
    cursor: pointer;
    font-size: 14px;
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

    img {
      width: 100%;
      display: block;
      object-fit: cover;
    }
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
