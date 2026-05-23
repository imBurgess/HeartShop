<template>
  <n-spin :show="loading" description="正在導向安全加密付款頁面...">
    <main class="cartPage">
      <section class="stepBar">
        <div class="step" @click="$router.push('/cart')">
          <div class="circle">01</div>
          <div class="label">MY CART<br />購物車清單</div>
        </div>

        <div class="step active">
          <div class="circle">02</div>
          <div class="label">ORDER CONFIRMATION<br />訂單確認&結帳</div>
        </div>

        <div class="step">
          <div class="circle">03</div>
          <div class="label">ORDER PAYMENT<br />訂單完成</div>
        </div>
      </section>

      <!-- 訂單商品資訊 -->
      <n-card class="block-card" title="Order Summary 訂單資訊" size="small">
        <div class="cart-summary-list">
          <div v-for="item in cartItems" :key="item.cartItemId" class="cart-summary-item">
            <div class="item-info">
              <n-image
                :src="item.image"
                :alt="item.name"
                width="50"
                height="50"
                object-fit="contain"
                preview-disabled
                class="summary-image"
              />
              <div class="item-details">
                <div class="name">{{ item.name }}</div>
                <div class="qty">數量：{{ item.quantity }}</div>
              </div>
            </div>
            <div class="item-price">
              ${{ (item.price * item.quantity).toLocaleString() }}
            </div>
          </div>
        </div>
        <div class="summary-total-row">
          <span>共 {{ totalQty }} 件商品</span>
          <span class="total-amount">小計：${{ storeTotalAmount.toLocaleString() }}</span>
        </div>
      </n-card>

      <!-- 物流與付款方式 -->
      <n-card class="block-card" title="Payment & Shipping 付款與物流" size="small">
        <n-form label-placement="top" :show-require-mark="false">
          <div class="two-cols">
            <n-form-item label="付款方式">
              <n-radio-group v-model:value="orderForm.paymentMethod">
                <n-space vertical>
                  <n-radio value="ecpay">信用卡 / ATM / 超商代碼 (綠界金流)</n-radio>
                </n-space>
              </n-radio-group>
            </n-form-item>
            <n-form-item label="物流方式">
              <n-radio-group v-model:value="orderForm.shippingMethod">
                <n-space vertical>
                  <n-radio value="home">宅配到府</n-radio>
                </n-space>
              </n-radio-group>
            </n-form-item>
          </div>
        </n-form>
      </n-card>

      <!-- 收件人資訊 -->
      <n-card class="block-card" title="Information 收貨人資訊" size="small">
        <n-form
          :model="orderForm"
          label-placement="top"
          label-width="auto"
          :show-require-mark="false"
        >
          <div class="two-cols">
            <n-form-item label="收件人姓名 Name">
              <n-input v-model:value="orderForm.receiverName" placeholder="請輸入姓名" />
            </n-form-item>
            <n-form-item label="收件人手機 Mobile">
              <n-input v-model:value="orderForm.receiverPhone" placeholder="請輸入手機號碼" />
            </n-form-item>
          </div>

          <n-form-item label="收件人地址 Address">
            <n-input
              v-model:value="orderForm.receiverAddress"
              placeholder="請輸入完整收件地址 (縣市/區/路名/巷弄/樓層)"
            />
          </n-form-item>

          <n-form-item label="備註 / 指定時間（選填）">
            <n-input
              v-model:value="orderForm.receiverNote"
              type="textarea"
              placeholder="有什麼想告訴我們的嗎？"
              :autosize="{ minRows: 3, maxRows: 6 }"
            />
          </n-form-item>

          <n-form-item>
            <n-checkbox v-model:checked="orderForm.agree">
              我已詳細閱讀並同意本站購物相關規範與隱私權政策。
            </n-checkbox>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 送出訂單 -->
      <section class="submitRow">
        <n-button
          round
          tertiary
          size="large"
          style="margin-right: 16px"
          :disabled="loading"
          @click="$router.push('/cart')"
        >
          回到購物車
        </n-button>

        <n-button
          round
          type="primary"
          size="large"
          :disabled="!orderForm.agree || cartItems.length === 0"
          :loading="loading"
          @click="handleSubmit"
        >
          確認並前往付款
        </n-button>
      </section>

      <!-- 動態建立綠界表單的容器 -->
      <div ref="ecpayFormContainer" style="display: none;"></div>
    </main>
  </n-spin>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { orderService } from "@/services/order";
import { storeToRefs } from "pinia";
import { useMessage } from "naive-ui";

const router = useRouter();
const cartStore = useCartStore();
const message = useMessage();
const { items: cartItems, totalQty, totalAmount: storeTotalAmount } = storeToRefs(cartStore);

const loading = ref(false);
const ecpayFormContainer = ref<HTMLElement | null>(null);

onMounted(() => {
  cartStore.fetchCart();
});

const orderForm = reactive({
  receiverName: "",
  receiverPhone: "",
  receiverAddress: "",
  receiverNote: "",
  paymentMethod: "ecpay",
  shippingMethod: "home",
  agree: false,
});

const handleSubmit = async () => {
  if (!orderForm.agree || loading.value) return;
  if (!orderForm.receiverName || !orderForm.receiverPhone || !orderForm.receiverAddress) {
    message.warning("請填寫完整的收件人資訊");
    return;
  }

  loading.value = true;
  try {
    const payload = {
      receiverName: orderForm.receiverName,
      receiverPhone: orderForm.receiverPhone,
      receiverAddress: orderForm.receiverAddress,
      receiverNote: orderForm.receiverNote,
      paymentMethod: orderForm.paymentMethod,
      shippingMethod: orderForm.shippingMethod,
      clientBackUrl: window.location.origin + "/cart/checkout"
    };

    const res = await orderService.placeOrder(payload);

    if (res && res.paymentUrl && res.ecpayParams) {
      // 成功取得綠界參數，動態建立 form 並送出
      cartStore.clearCart();
      localStorage.setItem("lastOrderNo", res.ecpayParams.MerchantTradeNo);
      submitECPayForm(res.paymentUrl, res.ecpayParams);
    } else {
      message.error("訂單建立失敗，請稍後再試");
      loading.value = false;
    }
  } catch (error: any) {
    message.error(error.message || "下單失敗");
    loading.value = false;
  }
};

const submitECPayForm = (actionUrl: string, params: Record<string, string>) => {
  if (!ecpayFormContainer.value) return;

  const form = document.createElement("form");
  form.method = "POST";
  form.action = actionUrl;

  for (const key in params) {
    if (Object.prototype.hasOwnProperty.call(params, key)) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = key;
      input.value = params[key];
      form.appendChild(input);
    }
  }

  ecpayFormContainer.value.appendChild(form);
  form.submit();
};
</script>

<style scoped>
.cartPage {
  max-width: 960px;
  margin: 40px auto 80px;
  padding: 0 16px;
}
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
  transition: all 0.3s ease;
}
.step:hover {
  opacity: 0.8;
}
.step.active {
  opacity: 1;
}
.circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #353535;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 6px;
}
.step.active .circle {
  background: #353535;
  color: #fff;
}
.label {
  text-align: center;
  font-size: 11px;
  line-height: 1.4;
  color: #353535;
}
.block-card {
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.two-cols {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.cart-summary-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.cart-summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-summary-item .item-info {
  display: flex;
  gap: 16px;
  align-items: center;
}
.summary-image {
  border-radius: 6px;
  background-color: #f9f9f9;
}
.cart-summary-item .item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.cart-summary-item .name {
  font-weight: 600;
  color: #353535;
  font-size: 14px;
}
.cart-summary-item .qty {
  font-size: 13px;
  color: #888;
}
.cart-summary-item .item-price {
  font-weight: 600;
  color: #353535;
  font-size: 15px;
}
.summary-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #353535;
}
.summary-total-row .total-amount {
  font-size: 18px;
}

.submitRow {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .stepBar {
    gap: 16px;
  }
  .two-cols {
    grid-template-columns: 1fr;
  }
}
</style>