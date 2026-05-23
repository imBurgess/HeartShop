<template>
  <main class="search-page">
    <!-- 左側：分類導覽 -->
    <aside class="leftSidebar" aria-label="商品分類">
      <h2>商品分類</h2>
      <n-menu :options="menuOptions" @update:value="handleMenuSelect" />
    </aside>

    <!-- 右側：搜尋結果 -->
    <section class="contentArea">
      <!-- 搜尋標題 -->
      <div class="search-hero">
        <form class="search-form" @submit.prevent="doSearch">
          <input
            v-model.trim="inputQuery"
            type="search"
            class="search-input"
            placeholder="搜尋商品..."
            autofocus
          />
          <button type="submit" class="search-btn">搜尋</button>
        </form>
        <p v-if="query" class="search-label">
          「<strong>{{ query }}</strong>」的搜尋結果
        </p>
      </div>

      <!-- 數量 + 排序 -->
      <div class="pdListHeader">
        <span class="count">
          <template v-if="!loading">共 {{ products.length }} 件商品</template>
          <template v-else>搜尋中...</template>
        </span>
        <label class="sort-label">
          排序：
          <select v-model="sortBy">
            <option value="newest">最新上架</option>
            <option value="price-low">價格由低到高</option>
            <option value="price-high">價格由高到低</option>
          </select>
        </label>
      </div>

      <!-- 載入 -->
      <div v-if="loading" class="state-box">
        <n-spin size="large" />
      </div>

      <!-- 無結果 -->
      <div v-else-if="!query" class="state-box">
        <p class="state-hint">請輸入關鍵字開始搜尋</p>
      </div>
      <div v-else-if="sortedProducts.length === 0" class="state-box">
        <n-empty :description="`找不到「${query}」相關商品`" />
        <p class="state-hint">試試看其他關鍵字，或瀏覽我們的商品分類</p>
      </div>

      <!-- 商品格 -->
      <div v-else class="pdListContainer">
        <article
          v-for="item in sortedProducts"
          :key="item.id"
          class="productCard"
          @click="router.push(`/product/${item.id}`)"
        >
          <div class="thumbWrapper">
            <img :src="item.image" :alt="item.name" />
            <span v-if="item.isNew" class="badge badge-new">新上架</span>
          </div>
          <div class="info">
            <h3 class="name">{{ item.name }}</h3>
            <div class="metaRow">
              <span v-if="item.discountPrice" class="price price-sale">NT${{ item.discountPrice.toLocaleString() }}</span>
              <span v-else class="price">NT${{ (item.price || 0).toLocaleString() }}</span>

              <div class="actions">
                <button class="qtyBtn" @click.stop="updateQty(item, -1)">-</button>
                <span class="qty">{{ item.quantity }}</span>
                <button class="qtyBtn" @click.stop="updateQty(item, 1)">+</button>
                <button class="cartBtn" @click.stop="addToCart(item)" aria-label="加入購物車"></button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { productService, type Product } from "@/services/product";
import { categoryService } from "@/services/category";
import { useCartStore } from "@/stores/cart";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const cartStore = useCartStore();

// ── 搜尋查詢 ──
const query = ref((route.query.q as string) || "");
const inputQuery = ref(query.value);

// ── 資料 ──
interface SearchItem extends Product {
  id: number;
  image: string;
  quantity: number;
}

const products = ref<SearchItem[]>([]);
const loading = ref(false);
const sortBy = ref("newest");

const sortedProducts = computed(() => {
  const list = [...products.value];
  if (sortBy.value === "price-low") {
    return list.sort((a, b) => (a.discountPrice || a.price || 0) - (b.discountPrice || b.price || 0));
  }
  if (sortBy.value === "price-high") {
    return list.sort((a, b) => (b.discountPrice || b.price || 0) - (a.discountPrice || a.price || 0));
  }
  return list.sort((a, b) =>
    new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
  );
});

const mapProduct = (p: Product): SearchItem => {
  const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  const rawImg = p.imageUrl || p.images?.[0] || "";
  const image = rawImg
    ? rawImg.startsWith("http") ? rawImg : `${apiBase}${rawImg}`
    : "/products/coat01.jpg";
  return { ...p, id: p.productId, image, quantity: 1 };
};

const fetchProducts = async (keyword: string) => {
  if (!keyword) {
    products.value = [];
    return;
  }
  loading.value = true;
  try {
    const res = await productService.getProducts({
      keyword,
      isActive: true,
      pageSize: 100,
    });
    const items: Product[] = res?.items ?? res ?? [];
    products.value = items.map(mapProduct);
  } catch {
    products.value = [];
  } finally {
    loading.value = false;
  }
};

// ── 購物車 ──
const updateQty = (item: SearchItem, delta: number) => {
  const next = (item.quantity || 1) + delta;
  if (next < 1) return;
  item.quantity = next;
};

const addToCart = async (item: SearchItem) => {
  const token = useCookie("token").value;
  if (!token) {
    message.warning("請先登入");
    router.push("/member");
    return;
  }
  const defaultSize = (item as any).sizeInfo
    ? (item as any).sizeInfo.split(/[,/]/)[0].trim()
    : "Free Size";
  const success = await cartStore.addToCart(item.id, defaultSize, item.quantity);
  if (success) {
    message.success(`已將 ${item.name} 加入購物車！`);
  } else {
    message.error("加入購物車失敗");
  }
};

// ── 搜尋動作 ──
const doSearch = () => {
  const q = inputQuery.value.trim();
  if (!q) return;
  router.push(`/search?q=${encodeURIComponent(q)}`);
};

// ── 同步路由 query → 搜尋 ──
watch(
  () => route.query.q,
  (val) => {
    query.value = (val as string) || "";
    inputQuery.value = query.value;
    fetchProducts(query.value);
  }
);

// ── 左側分類選單 ──
const menuOptions = ref([
  { label: "人氣商品推薦", key: "popular", route: "/shop/popular" },
]);

const handleMenuSelect = (key: string) => {
  const find = (opts: any[], k: string): any => {
    for (const o of opts) {
      if (o.key === k) return o;
      if (o.children) { const f = find(o.children, k); if (f) return f; }
    }
  };
  const opt = find(menuOptions.value, key);
  if (opt?.route) router.push(opt.route);
};

onMounted(async () => {
  fetchProducts(query.value);
  try {
    const res = await categoryService.getActiveCategories();
    const cats = Array.isArray(res) ? res : (res as any)?.data ?? [];
    menuOptions.value = [
      { label: "人氣商品推薦", key: "popular", route: "/shop/popular" },
      ...cats.map((c: any) => ({ label: `★ ${c.nameZh}`, key: c.slug, route: `/shop/${c.slug}` })),
    ];
  } catch {}
});
</script>

<style scoped lang="scss">
.search-page {
  display: grid;
  grid-template-columns: 260px 1fr;
  width: min(1200px, 94%);
  margin: 32px auto 80px;
  padding: 0;
  gap: 32px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* ── 左側欄 ── */
.leftSidebar {
  h2 {
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.06em;
    margin-bottom: 12px;
    margin-left: 25px;
    color: #353535;
  }
}

/* ── 右側內容 ── */
.contentArea {
  min-width: 0;
}

.search-hero {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  gap: 0;
  margin-bottom: 12px;
}

.search-input {
  flex: 1;
  height: 42px;
  padding: 0 14px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-right: none;
  border-radius: 4px 0 0 4px;
  outline: none;
  transition: border-color 0.2s;

  &:focus { border-color: #8a897c; }
}

.search-btn {
  height: 42px;
  padding: 0 20px;
  background: #353535;
  color: #fff;
  border: none;
  border-radius: 0 4px 4px 0;
  font-size: 13px;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: background 0.2s;

  &:hover { background: #5a5950; }
}

.search-label {
  font-size: 14px;
  color: #666;
  margin: 0;

  strong { color: #353535; }
}

/* ── 數量 / 排序 ── */
.pdListHeader {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.count {
  font-size: 13px;
  color: #888;
}

.sort-label {
  font-size: 13px;
  color: #555;

  select {
    margin-left: 6px;
    font-size: 13px;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 3px 8px;
    cursor: pointer;
  }
}

/* ── 狀態 ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0 40px;
  gap: 16px;
}

.state-hint {
  font-size: 13px;
  color: #aaa;
  margin: 0;
  text-align: center;
}

/* ── 商品格 ── */
.pdListContainer {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 1024px) { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  @media (max-width: 640px)  { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

.productCard {
  background: #ffffffc8;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #0000006b;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;

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

  .badge-new { background: #e60023; }
}

.info {
  padding: 8px 10px 10px;

  .name {
    font-size: 13px;
    font-weight: 500;
    margin: 0 0 6px;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .metaRow {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;

    .price {
      font-size: 13px;
      color: #353535;
      font-weight: 600;
    }

    .price-sale {
      color: #c0392b;
      font-weight: 700;
    }

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

      &:hover { background: #f3e9d8; }
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

      &:hover { background: #a57c63b3; }
    }
  }
}
</style>
