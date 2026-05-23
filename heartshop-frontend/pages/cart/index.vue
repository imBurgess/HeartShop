<template>
  <main class="cartPage">
    <section class="stepBar">
      <div class="step active">
        <div class="circle">01</div>
        <div class="label">MY CART<br />購物車清單</div>
      </div>

      <div class="step" @click="$router.push('/cart/confirm')">
        <div class="circle">02</div>
        <div class="label">ORDER CONFIRMATION<br />訂單確認&結帳</div>
      </div>

      <div class="step" @click="$router.push('/cart/checkout')">
        <div class="circle">03</div>
        <div class="label">ORDER PAYMENT<br />訂單完成</div>
      </div>
    </section>

    <!-- 標題 -->
    <h1 class="cartTitle">當前購物車</h1>

    <!-- 訂購內容卡片 -->
    <n-card class="cartCard" :bordered="false">
      <div class="cartCardHeader">訂購內容</div>

      <!-- 空購物車 -->
      <div v-if="cartItems.length === 0" class="emptyState">
        購物車目前沒有任何商品
      </div>

      <!-- 有商品 -->
      <div v-else class="cartItemList">
        <div v-for="item in cartItems" :key="item.cartItemId" class="cartItem">
          <!-- 商品圖片 -->
          <div class="itemThumb">
            <n-image
              :src="item.image"
              :alt="item.name"
              width="80"
              height="80"
              object-fit="contain"
              preview-disabled
            />
          </div>

          <!-- 商品資訊 -->
          <div class="itemMain">
            <div class="itemName">{{ item.name }}</div>

            <div class="itemPriceRow">
              <span class="itemPrice">
                ${{ item.price.toLocaleString() }}
              </span>
              <span class="itemTax">含稅</span>
            </div>

            <!-- 數量 -->
            <div class="itemQtyRow">
              <span>數量：</span>
              <n-space align="center" size="small">
                <n-button quaternary size="small" @click="decreaseQty(item)">
                  －
                </n-button>

                <n-input-number
                  v-model:value="item.quantity"
                  size="small"
                  :min="1"
                  :show-button="false"
                  style="width: 60px"
                />

                <n-button quaternary size="small" @click="increaseQty(item)">
                  ＋
                </n-button>
              </n-space>
            </div>

            <!-- 刪除 -->
            <div class="itemRemoveRow">
              <button class="removeBtn" @click="removeItem(item.cartItemId)">
                <span class="removeIcon">🗑</span>
                從購物車中刪除
              </button>
            </div>
          </div>

          <!-- 小計 -->
          <div class="itemSubtotal">
            小計<br />
            <span class="itemSubtotalPrice">
              ${{ (item.price * item.quantity).toLocaleString() }}
            </span>
          </div>
        </div>
      </div>
    </n-card>

    <!-- 總計區塊 -->
    <n-card
      v-if="cartItems.length > 0"
      class="cartSummary"
      size="small"
      :bordered="false"
    >
      <div class="summaryRow">
        <span>商品總數量：</span>
        <span>{{ totalQty }} 點</span>
      </div>
      <div class="summaryRow">
        <span>商品總金額：</span>
        <span>${{ totalAmount.toLocaleString() }} 含稅</span>
      </div>
    </n-card>

    <!-- 底部按鈕 -->
    <section class="cartActions" v-if="cartItems.length > 0">
      <n-space justify="center" size="large">
        <n-button round secondary @click="$router.push('/shop/popular')">
          繼續購物
        </n-button>
        <n-button round type="primary" @click="$router.push('/cart/confirm')">
          繼續購買程序
        </n-button>
      </n-space>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useCartStore } from "@/stores/cart";
import { storeToRefs } from "pinia";

const cartStore = useCartStore();
const { items: cartItems, totalQty, totalAmount } = storeToRefs(cartStore);

onMounted(() => {
  cartStore.fetchCart();
});

const increaseQty = (item: any) => {
  cartStore.updateQuantity(item.cartItemId, item.quantity + 1);
};

const decreaseQty = (item: any) => {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.cartItemId, item.quantity - 1);
  }
};

const removeItem = (id: number) => {
  cartStore.removeItem(id);
};
</script>

<style scoped>
/* STEP bar */
.stepBar {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 24px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  opacity: 0.6;
}

.step.active {
  opacity: 1;
}

.circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 6px;
}

.step.active .circle {
  background: #555;
  color: #fff;
}

.label {
  text-align: center;
  font-size: 11px;
  line-height: 1.4;
}

.cartPage {
  max-width: 960px;
  margin: 40px auto 80px;
  padding: 0 16px;
}

.cartTitle {
  text-align: center;
  font-size: 24px;
  letter-spacing: 0.25em;
  margin-bottom: 32px;
}

.cartCard {
  margin-bottom: 24px;
  border: 1px solid #f0f0f0;
  padding: 0;
}

.cartCardHeader {
  padding: 12px 24px;
  background-color: #f7f7f7;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

.emptyState {
  padding: 32px 24px;
  text-align: center;
  color: #353535;
}

.cartItemList {
  padding: 16px 24px 8px;
}

.cartItem {
  display: grid;
  grid-template-columns: 100px 1fr 140px;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.cartItem:last-child {
  border-bottom: none;
}

.itemThumb {
  display: flex;
  align-items: center;
  justify-content: center;
}

.itemMain {
  font-size: 14px;
}

.itemName {
  font-weight: 600;
  margin-bottom: 4px;
}

.itemPriceRow {
  margin-bottom: 8px;
}

.itemPrice {
  font-size: 14px;
}

.itemTax {
  font-size: 12px;
  color: #353535;
  margin-left: 4px;
}

.itemQtyRow {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.itemRemoveRow {
  font-size: 12px;
  margin-top: 6px;
}

.removeBtn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  font-size: 12px;
  color: #c0392b;
  background: transparent;
  border: 1px solid #c0392b;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.18s, color 0.18s;

  &:hover {
    background: #c0392b;
    color: #fff;
  }
}

.removeIcon {
  font-size: 13px;
  line-height: 1;
}

.itemSubtotal {
  text-align: right;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.itemSubtotalPrice {
  font-size: 16px;
  font-weight: 600;
}

.cartSummary {
  max-width: 420px;
  margin-left: auto;
  border: 1px solid #f0f0f0;
}

.summaryRow {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 4px 0;
}

.summaryRow:last-child {
  font-weight: 600;
}

.cartActions {
  margin-top: 32px;
}

/* 手機版調整 */
@media (max-width: 768px) {
  .cartItem {
    grid-template-columns: 80px 1fr;
    grid-template-rows: auto auto;
  }
  .itemSubtotal {
    grid-column: 2 / 3;
    align-items: flex-end;
    margin-top: 4px;
  }
}
</style>
