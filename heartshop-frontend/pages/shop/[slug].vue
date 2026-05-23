<template>
  <main class="categoryPage">
    <!-- 左邊：側邊欄 -->
    <aside class="leftSidebar" aria-label="商品分類">
      <h2>商品分類</h2>
      <ul class="sideMenu">
        <li>
          <button
            class="sideMenuItem"
            :class="{ active: slug === 'popular' }"
            @click="router.push('/shop/popular')"
          >人氣商品推薦</button>
        </li>
        <li v-for="parent in menuTree" :key="parent.slug">
          <div class="sideMenuParentRow">
            <button
              class="sideMenuItem"
              :class="{ active: slug === parent.slug }"
              @click="router.push(`/shop/${parent.slug}`)"
            >★ {{ parent.nameZh }}</button>
            <button
              v-if="parent.children.length > 0"
              class="sideToggleBtn"
              @click="toggleParent(parent.slug)"
              :aria-expanded="!!expandedParents[parent.slug]"
            >{{ expandedParents[parent.slug] ? '▲' : '▼' }}</button>
          </div>
          <ul v-if="parent.children.length > 0 && expandedParents[parent.slug]" class="sideSubMenu">
            <li v-for="sub in parent.children" :key="sub.slug">
              <button
                class="sideMenuItem sideSubItem"
                :class="{ active: slug === sub.slug }"
                @click="router.push(`/shop/${sub.slug}`)"
              >★ {{ sub.nameZh }}</button>
            </li>
          </ul>
        </li>
      </ul>
    </aside>

    <!-- 右邊：內容區 -->
    <section class="contentArea">
      <!-- Banner -->
      <div class="mainImg">
        <img :src="currentBanner.src" :alt="currentBanner.alt" />
      </div>
      <!-- Tag 列 -->
      <div class="tagBar">
        <button
          v-for="tag in availableTags"
          :key="tag"
          class="tagChip"
          :class="{ active: tag === selectedTag }"
          @click="selectedTag = tag"
        >
          {{ tag }}
        </button>
      </div>

      <!-- 標題 + 商品數量 / 排序 -->
      <div class="pdListHeader">
        <div class="pdListTitle">
          <span class="count">共 {{ filteredProducts.length }} 件商品</span>
        </div>

        <div class="sortArea">
          <label>
            排序：
            <select v-model="sortBy">
              <option value="newest">最新上架</option>
              <option value="price-low">價格由低到高</option>
              <option value="price-high">價格由高到低</option>
            </select>
          </label>
        </div>
      </div>

      <!--商品區-->
      <div class="pdListContainer">
        <article
          v-for="item in filteredProducts"
          :key="item.id"
          class="productCard"
          @click="goProductDetail(item)"
        >
          <div class="thumbWrapper">
            <img :src="item.image" :alt="item.name" />
            <span v-if="item.isNew" class="badge badge-new">新上架</span>
          </div>
          <div class="info">
            <h3 class="name">{{ item.name }}</h3>

            <div class="metaRow">
              <span class="price">NT${{ item.price }}</span>

              <div class="actions">
                <button class="qtyBtn" @click.stop="updateQty(item, -1)">
                  -
                </button>
                <span class="qty">{{ item.quantity }}</span>
                <button class="qtyBtn" @click.stop="updateQty(item, 1)">
                  +
                </button>

                <button
                  class="cartBtn"
                  @click.stop="addToCart(item)"
                  aria-label="加入購物車"
                ></button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useMessage } from "naive-ui"; // 如有安裝 Naive UI
import { useCartStore } from "@/stores/cart";

const route = useRoute();
const router = useRouter();
const message = useMessage();

// 目前網址上的 slug
const slug = computed(() => route.params.slug);

// Banner 設定表（保留作為備用）
const bannerMap = {
  popular: { src: "/banners/popular.webp", alt: "人氣商品推薦 Banner" },
};

const defaultBanner = {
  src: "/banners/defaultBanner.webp",
  alt: "預設 Banner",
};

const currentBanner = computed(() => {
  if (slug.value === "popular") {
    return bannerMap.popular || defaultBanner;
  }

  const category = categories.value.find((c) => c.slug === slug.value);

  // 子分類若無自己的 banner，繼承父分類的 banner
  let bannerUrl = category?.bannerUrl;
  if (!bannerUrl && category?.parentId) {
    const parent = categories.value.find((c) => c.categoryId === category.parentId);
    bannerUrl = parent?.bannerUrl;
  }

  if (bannerUrl) {
    const apiHost = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    return {
      src: bannerUrl.startsWith("http") ? bannerUrl : `${apiHost}${bannerUrl}`,
      alt: `${category?.nameZh} Banner`,
    };
  }

  return defaultBanner;
});

// ---------------- 側邊欄折疊狀態 ----------------
const expandedParents = ref({});

const toggleParent = (slug) => {
  expandedParents.value[slug] = !expandedParents.value[slug];
};

// ---------------- API 資料 fetching ----------------
const categories = ref([]);
const products = ref([]);
const loading = ref(false);

// 階層化分類樹（父分類 + 其子分類）
const menuTree = computed(() => {
  const parents = categories.value.filter((c) => !c.parentId);
  const subs = categories.value.filter((c) => c.parentId);
  return parents.map((parent) => ({
    ...parent,
    children: subs.filter((c) => c.parentId === parent.categoryId),
  }));
});

// 載入所有分類 (用來將 slug 對應到 categoryId)
const fetchCategories = async () => {
  try {
    const res = await $fetch("/api/categories");
    if (res.code === "0000") {
      categories.value = res.data || [];

      // 若目前 slug 是子分類，自動展開父分類
      const currentSub = categories.value.find((c) => c.slug === slug.value && c.parentId);
      if (currentSub) {
        expandedParents.value[
          categories.value.find((c) => c.categoryId === currentSub.parentId)?.slug
        ] = true;
      }
    }
  } catch (err) {
    console.error("載入分類失敗", err);
  }
};

// 載入商品
const fetchProducts = async () => {
  loading.value = true;
  products.value = [];

  try {
    const params = {
      isActive: true,
      page: 1,
      pageSize: 100, // 暫時抓多一點
      sortBy: "sort_order",
      sortOrder: "ASC",
    };

    // 根據 slug 決定查詢參數
    if (slug.value === "popular") {
      // 人氣商品：依瀏覽次數排序 (如果後端支援 view_count，否則依預設)
      params.sortBy = "view_count"; // 需確認後端是否支援 snake_case mapping，通常通用
      params.sortOrder = "DESC";
    } else if (slug.value === "new") {
      params.isNew = true;
    } else {
      // 嘗試從分類列表中找對應的 slug
      // 先確保分類已載入
      if (categories.value.length === 0) {
        await fetchCategories();
      }

      const cat = categories.value.find((c) => c.slug === slug.value);
      if (cat) {
        params.categoryId = cat.categoryId;
      } else {
        // 如果找不到對應分類且不是特殊頁面，可能就是無效 slug 或尚未建立的分類
        // 這裡可以選擇不撈或是撈全部，或是顯示空
        console.warn(`找不到 slug: ${slug.value} 對應的分類`);
      }
    }

    const res = await $fetch("/api/products", { params });
    // 修正：後端回傳 code: "0000"
    if (res.code === "0000") {
      // 後端回傳可能是 Map 或物件，根據 ProductController 是回傳 Map<String, Object> (items, total...)
      // 假設結構是 res.data.items 或 res.data.products
      const items = res.data.items || res.data.products || [];

      // 資料處理：tags 可能是陣列或字串, 處理圖片
      products.value = items.map((p) => ({
        ...p,
        id: p.productId, // 前端 template 用 id
        // tags 可能已經是陣列(後端DTO轉換過)或字串,需判斷
        tags: Array.isArray(p.tags)
          ? p.tags
          : p.tags
            ? p.tags.split(",").filter((t) => t)
            : [],
        // 使用後端回傳的 imageUrl 或 images[0]，否則給預設圖
        image:
          p.imageUrl || (p.images && p.images[0]) || "/products/coat01.jpg",
        quantity: 1, // 購物車用
      }));
    }
  } catch (err) {
    console.error("載入商品失敗", err);
  } finally {
    loading.value = false;
  }
};

// 監聽路由變化重新載入
watch(
  () => route.params.slug,
  () => {
    fetchProducts();
  },
);

onMounted(async () => {
  await fetchCategories();
  fetchProducts();
});

//購物車相關邏輯
const updateQty = (item, delta) => {
  const next = (item.quantity || 1) + delta;
  if (next < 1) return;
  item.quantity = next;
};

const addToCart = async (item) => {
  const token = useCookie("token").value;
  if (!token) {
    message.warning("請先登入");
    router.push("/member");
    return;
  }

  const defaultSize = item.sizeInfo
    ? item.sizeInfo.split(/[,/]/)[0].trim()
    : "Free Size";

  const cartStore = useCartStore();
  const success = await cartStore.addToCart(item.id, defaultSize, item.quantity);

  if (success) {
    message.success(`已將 ${item.name} 加入購物車！`);
  } else {
    message.error("加入購物車失敗");
  }
};

const goProductDetail = (item) => {
  router.push(`/product/${item.id}`);
};

// ---------------- Tag 列 與 前端篩選 ----------------
const ALL_TAG = "全部";
const selectedTag = ref(ALL_TAG);
const sortBy = ref("newest");

// 這個分類底下有哪些 tag（從 fetch 回來的商品資料自動算出）
const availableTags = computed(() => {
  const set = new Set();
  products.value.forEach((p) => {
    p.tags?.forEach((t) => set.add(t));
  });
  return [ALL_TAG, ...Array.from(set)];
});

// 最終要呈現的商品（依 tag 篩選 + 排序）
const filteredProducts = computed(() => {
  let list = selectedTag.value === ALL_TAG
    ? [...products.value]
    : products.value.filter((p) => p.tags?.includes(selectedTag.value));

  if (sortBy.value === "price-low") {
    list.sort((a, b) => (a.discountPrice || a.price) - (b.discountPrice || b.price));
  } else if (sortBy.value === "price-high") {
    list.sort((a, b) => (b.discountPrice || b.price) - (a.discountPrice || a.price));
  } else {
    // newest: 依 createdAt 降冪
    list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  return list;
});
</script>

<style lang="scss">
.categoryPage {
  padding: 32px 0 60px;
  width: min(1200px, 94%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 32px;
}

.leftSidebar h2 {
  margin-left: 25px;
}

.sideMenu,
.sideSubMenu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sideMenuParentRow {
  display: flex;
  align-items: center;
}

.sideMenuItem {
  flex: 1;
  text-align: left;
  background: none;
  border: none;
  padding: 10px 25px;
  cursor: pointer;
  font-size: 14px;
  color: inherit;
  transition: color 0.2s;

  &:hover {
    color: #5a8a6a;
  }

  &.active {
    color: #5a8a6a;
    font-weight: 600;
  }
}

.sideSubItem {
  font-size: 13px;
  padding-left: 40px;
}

.sideToggleBtn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 10px 16px 10px 0;
  font-size: 9px;
  color: #aaa;

  &:hover {
    color: #5a8a6a;
  }
}

/* Banner */
.mainImg {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;

  img {
    width: 100%;
    height: 100%;
  }

  h1 {
    font-size: 28px;
    margin: 0 0 4px;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

/* Tag 列 */
.tagBar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tagChip {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #050505;
  background: #ffffff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;

  &.active {
    background: #633f29b3;
    color: #fff;
  }

  &:hover {
    background: #a57c63b3;
  }
}

/* 標題 + 排序那一排 */
.pdListHeader {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .pdListTitle {
    display: flex;
    align-items: baseline;
    gap: 8px;

    .count {
      font-size: 13px;
      color: #353535;
    }
  }

  .sortArea {
    font-size: 13px;

    select {
      margin-left: 4px;
      padding: 4px 8px;
      border-radius: 4px;
      border: 1px solid #ccc;
      font-size: 13px;
    }
  }
}

/* 商品區 */
.pdListContainer {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

/* 商品卡片 */
.productCard {
  background: #ffffffc8;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #0000006b;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.thumbWrapper {
  position: relative;
  width: 100%;
  padding-top: 120%;
  overflow: hidden;

  img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .badge {
    position: absolute;
    left: 8px;
    top: 8px;
    padding: 2px 6px;
    font-size: 11px;
    border-radius: 4px;
    color: #fff;
  }

  .badge-new {
    background: #e60023;
  }
}

.productCard .info {
  padding: 8px 10px 10px;

  .name {
    font-size: 13px;
    margin: 0 0 6px;
    line-height: 1.4;
  }

  .metaRow {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;

    .actions {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .qtyBtn {
      width: 24px;
      height: 24px;
      border-radius: 4px;
      border: 1px solid #b4a582;
      background: #ffffff;
      cursor: pointer;
      font-size: 14px;
      line-height: 1;
      padding: 0;

      &:hover {
        background: #f3e9d8;
      }
    }

    .qty {
      min-width: 20px;
      text-align: center;
      font-size: 13px;
    }

    .cartBtn {
      width: 28px;
      height: 28px;
      margin-left: 5px;
      padding: 0;
      border-radius: 50%;
      border: none;
      cursor: pointer;
      background-color: #ff0000;
      position: relative;

      &::before {
        content: "";
        position: absolute;
        inset: 0;
        margin: auto;
        width: 16px;
        height: 16px;
        background-color: #ffffff;
        -webkit-mask-image: url("~/assets/img/cart.svg");
        mask-image: url("~/assets/img/cart.svg");
        -webkit-mask-repeat: no-repeat;
        mask-repeat: no-repeat;
        -webkit-mask-position: center;
        mask-position: center;
        -webkit-mask-size: contain;
        mask-size: contain;
      }

      &:hover {
        background: #a57c63b3;
      }
    }
  }
}

/* 簡單的 RWD：螢幕變窄就變 3 欄 / 2 欄 */
@media (max-width: 1024px) {
  .pdListContainer {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .categoryPage {
    grid-template-columns: 1fr;
  }

  .pdListContainer {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
