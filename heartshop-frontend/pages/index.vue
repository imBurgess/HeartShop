<template>
  <div>
    <Particles />
    <main>
      <!--廣告區-->
      <div class="banner-carousel">
        <n-carousel show-arrow autoplay v-if="carouselBlocks.length > 0">
          <div
            v-for="block in carouselBlocks"
            :key="block.blockId"
            class="carousel-item"
            @click="handleCarouselClick(block)"
          >
            <img
              class="carousel-img"
              :src="getFullImageUrl(block.imageUrl)"
              :alt="block.title || 'Banner'"
            />
          </div>

          <template #arrow="{ prev, next }">
            <div class="custom-arrow">
              <button type="button" class="custom-arrow--left" @click="prev">
                <n-icon><ArrowBack /></n-icon>
              </button>
              <button type="button" class="custom-arrow--right" @click="next">
                <n-icon><ArrowForward /></n-icon>
              </button>
            </div>
          </template>

          <template #dots="{ total, currentIndex, to }">
            <ul class="custom-dots">
              <li
                v-for="index of total"
                :key="index"
                :class="{ ['is-active']: currentIndex === index - 1 }"
                @click="to(index - 1)"
              />
            </ul>
          </template>
        </n-carousel>

        <!-- 如果沒有輪播圖資料,顯示佔位符 -->
        <div v-else class="carousel-placeholder">
          <p>暫無輪播圖資料</p>
        </div>
      </div>

      <!-- 現已上架商品 -->
      <section class="active-products">
        <div class="section-header">
          <h2 class="section-title">現已上架商品</h2>
        </div>

        <div v-if="activeProducts.length > 0" class="products-grid">
          <div
            v-for="product in activeProducts"
            :key="product.productId"
            class="product-card"
            @click="navigateToProduct(product.productId)"
          >
            <div class="product-image-wrapper">
              <img
                :src="getProductImageUrl(product.imageUrl)"
                :alt="product.name"
                class="product-image"
              />
              <div v-if="product.isNew" class="badge new-badge">新上架</div>
            </div>

            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>

              <div class="meta-row">
                <span class="price"
                  >NT${{ product.price?.toLocaleString() || "0" }}</span
                >

                <div class="actions">
                  <button class="qty-btn" @click.stop="updateQty(product, -1)">
                    -
                  </button>
                  <span class="qty">{{ product.quantity || 1 }}</span>
                  <button class="qty-btn" @click.stop="updateQty(product, 1)">
                    +
                  </button>

                  <button
                    class="cart-btn"
                    @click.stop="addToCart(product)"
                    aria-label="加入購物車"
                  ></button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="no-products">
          <p>暫無上架商品</p>
        </div>

        <div class="view-all-wrapper">
          <a href="/shop/popular" class="view-all-button">ALL ITEMS</a>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import Particles from "@/components/Particles.vue";
import { ArrowBack, ArrowForward } from "@vicons/ionicons5";
import { homeBlockService, type HomeBlockDTO } from "@/services/homeBlock";
import { productService, type Product } from "@/services/product";

// 輪播廣告
const carouselBlocks = ref<HomeBlockDTO[]>([]);

// 現已上架商品
const activeProducts = ref<Product[]>([]);

// 圖片 URL 轉換函數
const getFullImageUrl = (imageUrl?: string): string => {
  console.log("[getFullImageUrl] 原始 URL:", imageUrl);
  if (!imageUrl) return "";

  // 如果已經是完整 URL,直接返回
  if (imageUrl.startsWith("http")) {
    console.log("[getFullImageUrl] 已是完整 URL，直接返回");
    return imageUrl;
  }

  // 如果是以 / 開頭的絕對路徑
  if (imageUrl.startsWith("/")) {
    const baseUrl = "http://localhost:8080";
    // 特別處理：如果是 /uploads 開頭但不含 /api，需要加上 /api
    if (imageUrl.startsWith("/uploads")) {
      const fullUrl = `${baseUrl}/api${imageUrl}`;
      console.log("[getFullImageUrl] /uploads 路徑，加上 /api:", fullUrl);
      return fullUrl;
    }
    // 其他以 / 開頭的路徑（如已含 /api）直接拼接
    const fullUrl = `${baseUrl}${imageUrl}`;
    console.log("[getFullImageUrl] 絕對路徑，完整 URL:", fullUrl);
    return fullUrl;
  }

  // 相對路徑，加上完整路徑
  const baseUrl = "http://localhost:8080/api";
  const fullUrl = `${baseUrl}/${imageUrl}`;
  console.log("[getFullImageUrl] 相對路徑，完整 URL:", fullUrl);
  return fullUrl;
};

// 商品圖片 URL 轉換
const getProductImageUrl = (imageUrl?: string): string => {
  console.log("[DEBUG] 原始 imageUrl:", imageUrl);
  if (!imageUrl) {
    console.log("[DEBUG] imageUrl 為空，使用預設圖片");
    return "/images/placeholder-product.jpg";
  }
  const fullUrl = getFullImageUrl(imageUrl);
  console.log("[DEBUG] 完整圖片 URL:", fullUrl);
  return fullUrl;
};

// 導航到商品詳情頁
const navigateToProduct = (productId: number) => {
  window.location.href = `/product/${productId}`;
};

// 購物車相關邏輯
const updateQty = (item: any, delta: number) => {
  const next = (item.quantity || 1) + delta;
  if (next < 1) return;
  item.quantity = next;
};

const addToCart = (item: any) => {
  console.log("加入購物車", item.name, "數量", item.quantity);
  // 未來可以整合購物車 API
  // window.alert(`已加入購物車：${item.name} x ${item.quantity}`);
};

// 輪播圖點擊處理
const handleCarouselClick = (block: HomeBlockDTO) => {
  if (block.linkUrl) {
    // 如果有設定連結,導向該連結
    window.location.href = block.linkUrl;
  }
};

// 載入上架商品
const loadActiveProducts = async () => {
  console.log("[DEBUG] 開始載入上架商品...");
  try {
    console.log("[DEBUG] 呼叫 productService.getProducts");
    const response = await productService.getProducts({
      isActive: true,
      pageSize: 8, // 顯示 8 個商品
    });

    console.log("[DEBUG] API 回應:", response);

    // 注意：apiFetch 已經解包 response.data，所以這裡的 response 就是後端的 data 物件
    // 後端格式: { items: [...], total: X, page: X, pageSize: X }
    if (response && response.items) {
      console.log("[DEBUG] 成功取得商品數量:", response.items.length);
      console.log("[DEBUG] 商品詳細資料:", response.items);
      // 顯示每個商品的 imageUrl
      response.items.forEach((product: any, index: number) => {
        console.log(`[DEBUG] 商品 ${index + 1}:`, {
          name: product.name,
          imageUrl: product.imageUrl,
          price: product.price,
        });
      });
      activeProducts.value = response.items.map((product: any) => ({
        ...product,
        quantity: 1, // 購物車用數量
      }));
    } else {
      console.warn("[DEBUG] API 回應格式不正確或無資料:", response);
    }
  } catch (error) {
    console.error("[ERROR] 載入上架商品失敗:", error);
  }
};

// 載入首頁資料
onMounted(async () => {
  try {
    // 載入輪播廣告
    const carousel = await homeBlockService
      .getBlocksByType("CAROUSEL")
      .catch(() => []);

    // 輪播廣告
    if (carousel && carousel.length > 0) {
      console.log("[DEBUG] 輪播圖數量:", carousel.length);
      carousel.forEach((block: any, index: number) => {
        console.log(`[DEBUG] 輪播圖 ${index + 1}:`, {
          title: block.title,
          imageUrl: block.imageUrl,
        });
      });
      carouselBlocks.value = carousel;
    }

    // 載入上架商品
    await loadActiveProducts();
  } catch (error) {
    console.error("載入首頁資料失敗:", error);
  }
});
</script>
<style lang="scss">
@use "@/assets/scss/home/banner" as *;
@use "@/assets/scss/home/activeproducts" as *;

@use "@/assets/scss/home/line" as *;
</style>
