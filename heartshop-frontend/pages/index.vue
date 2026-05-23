<template>
  <div>
    <main>
      <div class="banner-wrapper">
        <div class="banner-carousel">
          <n-carousel
            show-arrow
            autoplay
            v-if="carouselBlocks.length > 0"
            effect="fade"
            class="custom-carousel"
          >
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

          <div v-else class="carousel-placeholder">
            <p>暫無輪播圖資料</p>
          </div>
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
          <NuxtLink to="/shop/popular" class="view-all-button">ALL ITEMS</NuxtLink>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ArrowBack, ArrowForward } from "@vicons/ionicons5";
import { homeBlockService, type HomeBlockDTO } from "@/services/homeBlock";
import { productService, type Product } from "@/services/product";
import { useCartStore } from "@/stores/cart";
import { useMessage } from "naive-ui";

const cartStore = useCartStore();
const message = useMessage();

// 輪播廣告
const carouselBlocks = ref<HomeBlockDTO[]>([]);

// 現已上架商品
const activeProducts = ref<Product[]>([]);

// 圖片 URL 轉換函數
const getFullImageUrl = (imageUrl?: string): string => {
  if (!imageUrl) return "";
  if (imageUrl.startsWith("http")) return imageUrl;

  const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  if (imageUrl.startsWith("/uploads")) return `${baseUrl}/api${imageUrl}`;
  if (imageUrl.startsWith("/")) return `${baseUrl}${imageUrl}`;

  return `${baseUrl}/api/${imageUrl}`;
};

// 商品圖片 URL 轉換
const getProductImageUrl = (imageUrl?: string): string => {
  if (!imageUrl) return "/images/placeholder-product.jpg";
  return getFullImageUrl(imageUrl);
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

const addToCart = async (item: any) => {
  const defaultSize = item.sizeInfo
    ? item.sizeInfo.split(/[,/]/)[0].trim()
    : "Free Size";
  const success = await cartStore.addToCart(item.productId, defaultSize, item.quantity || 1);
  
  if (success) {
    message.success(`已將 ${item.name} 加入購物車！`);
  } else {
    message.error("加入購物車失敗，請稍後再試或確認是否已登入");
  }
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
  try {
    const response = await productService.getProducts({
      isActive: true,
      pageSize: 8,
    });
    if (response && response.items) {
      activeProducts.value = response.items.map((product: any) => ({
        ...product,
        quantity: 1,
      }));
    }
  } catch (error) {
    console.error("載入上架商品失敗:", error);
  }
};

// 載入首頁資料
onMounted(async () => {
  try {
    // 載入輪播廣告
    const carousel = await homeBlockService
      .getBlocksByType("CAROUSEL")
      .catch(() => []);

    if (carousel && carousel.length > 0) {
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
</style>
