<template>
  <main class="cartPage">
    <!-- 步驟列 -->
    <section class="stepBar">
      <div class="step">
        <div class="circle">01</div>
        <div class="label">MY CART<br />購物車清單</div>
      </div>
      <div class="stepLine" />
      <div class="step">
        <div class="circle">02</div>
        <div class="label">ORDER CONFIRMATION<br />訂單確認&amp;結帳</div>
      </div>
      <div class="stepLine" />
      <div class="step active">
        <div class="circle">03</div>
        <div class="label">ORDER COMPLETE<br />訂單完成</div>
      </div>
    </section>

    <!-- 載入中 -->
    <div v-if="loading" class="centerBox">
      <n-spin size="large" />
      <p class="loadingText">訂單確認中，請稍候…</p>
    </div>

    <!-- 訂單資料 -->
    <template v-else-if="order">
      <!-- 結果標題 -->
      <div class="resultHeader" :class="order.status === 'PAID' ? 'paid' : 'pending'">
        <div class="resultIcon">{{ order.status === 'PAID' ? '✓' : '!' }}</div>
        <h2 class="resultTitle">
          {{ order.status === 'PAID' ? '付款成功，感謝您的購買！' : '訂單已成立，等待付款確認' }}
        </h2>
        <p class="resultSub">
          {{ order.status === 'PAID'
            ? '您的訂單已確認付款，我們將盡快為您安排出貨。'
            : '若已完成付款，系統確認後將自動更新狀態。' }}
        </p>
      </div>

      <!-- 訂單資訊卡 -->
      <n-card class="infoCard" size="small">
        <div class="infoGrid">
          <div class="infoRow">
            <span class="infoLabel">訂單編號</span>
            <span class="infoValue mono">{{ order.orderNo }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">訂單狀態</span>
            <n-tag :type="order.status === 'PAID' ? 'success' : 'warning'" size="small">
              {{ statusLabel(order.status) }}
            </n-tag>
          </div>
          <div class="infoRow">
            <span class="infoLabel">付款方式</span>
            <span class="infoValue">{{ paymentLabel(order.paymentMethod) }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">物流方式</span>
            <span class="infoValue">{{ shippingLabel(order.shippingMethod) }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">訂單金額</span>
            <span class="infoValue price">NT $ {{ order.totalAmount?.toLocaleString() }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">建立時間</span>
            <span class="infoValue">{{ formatDate(order.createdAt) }}</span>
          </div>
        </div>
      </n-card>

      <!-- 收件資訊卡 -->
      <n-card class="infoCard" size="small" title="收件人資訊">
        <div class="infoGrid">
          <div class="infoRow">
            <span class="infoLabel">姓名</span>
            <span class="infoValue">{{ order.receiverName }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">手機</span>
            <span class="infoValue">{{ order.receiverPhone }}</span>
          </div>
          <div class="infoRow">
            <span class="infoLabel">地址</span>
            <span class="infoValue">{{ order.receiverAddress }}</span>
          </div>
          <div v-if="order.receiverNote" class="infoRow">
            <span class="infoLabel">備註</span>
            <span class="infoValue">{{ order.receiverNote }}</span>
          </div>
        </div>
      </n-card>

      <!-- 操作按鈕 -->
      <div class="actionRow">
        <n-button round size="large" @click="$router.push('/')">回到首頁</n-button>
        <n-button round type="primary" size="large" @click="$router.push('/shop/popular')">繼續購物</n-button>
      </div>
    </template>

    <!-- 找不到訂單 -->
    <div v-else class="centerBox">
      <n-result status="error" title="找不到訂單" description="發生錯誤或訂單連結已失效，請聯絡客服。">
        <template #footer>
          <n-button @click="$router.push('/')">回首頁</n-button>
        </template>
      </n-result>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderService, type Order } from '@/services/order'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(true)
const order = ref<Order | null>(null)

onMounted(async () => {
  const orderNo = (route.query.orderNo as string) || localStorage.getItem('lastOrderNo')
  if (!orderNo) {
    loading.value = false
    message.error('訂單編號遺失')
    return
  }

  try {
    const data = await orderService.getOrder(orderNo)
    order.value = data
    localStorage.removeItem('lastOrderNo')
  } catch (error: any) {
    message.error(error.message || '載入訂單失敗')
  } finally {
    loading.value = false
  }
})

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    PAID: '已付款',
    pending: '待付款',
    FAILED: '付款失敗',
    SHIPPED: '已出貨',
    COMPLETED: '已完成',
  }
  return map[status] ?? status
}

const paymentLabel = (method: string) => {
  const map: Record<string, string> = {
    ecpay: '綠界金流（信用卡 / ATM / 超商）',
    credit_card: '信用卡',
  }
  return map[method] ?? method
}

const shippingLabel = (method: string) => {
  const map: Record<string, string> = {
    home: '宅配到府',
    store: '超商取貨',
  }
  return map[method] ?? method
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

<style scoped>
.cartPage {
  max-width: 720px;
  margin: 40px auto 80px;
  padding: 0 16px;
}

/* ── 步驟列 ── */
.stepBar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 40px;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0.45;
}
.step.active { opacity: 1; }
.stepLine {
  flex: 1;
  max-width: 60px;
  height: 1px;
  background: #ccc;
  margin: 0 8px;
  margin-bottom: 20px;
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
  font-size: 13px;
}
.step.active .circle {
  background: #353535;
  color: #fff;
}
.label {
  text-align: center;
  font-size: 10px;
  line-height: 1.4;
  color: #353535;
  white-space: nowrap;
}

/* ── 結果標題 ── */
.resultHeader {
  text-align: center;
  padding: 36px 24px 28px;
  border-radius: 8px;
  margin-bottom: 24px;
}
.resultHeader.paid   { background: #f0faf4; }
.resultHeader.pending { background: #fffbe6; }

.resultIcon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
  margin: 0 auto 16px;
}
.paid .resultIcon   { background: #18a058; color: #fff; }
.pending .resultIcon { background: #f0a020; color: #fff; }

.resultTitle {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
}
.resultSub {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* ── 資訊卡 ── */
.infoCard {
  margin-bottom: 16px;
  border: 1px solid #eee;
}
.infoGrid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.infoRow {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
.infoLabel {
  width: 80px;
  flex-shrink: 0;
  color: #888;
  font-size: 13px;
}
.infoValue { color: #1a1a1a; }
.infoValue.mono { font-family: monospace; letter-spacing: 0.04em; }
.infoValue.price { font-weight: 600; font-size: 16px; color: #353535; }

/* ── 載入 ── */
.centerBox {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  gap: 16px;
}
.loadingText { color: #888; font-size: 14px; }

/* ── 按鈕列 ── */
.actionRow {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

@media (max-width: 600px) {
  .stepBar { gap: 4px; }
  .stepLine { max-width: 24px; }
  .label { font-size: 9px; }
  .actionRow { flex-direction: column; align-items: center; }
}
</style>
