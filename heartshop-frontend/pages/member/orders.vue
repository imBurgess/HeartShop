<template>
  <main class="member-page">
    <!-- 隱藏的綠界表單容器 -->
    <div ref="ecpayFormContainer" style="display:none" />

    <n-layout class="member-layout">
      <n-layout has-sider>
        <!-- 左側：會員選單 -->
        <n-layout-sider width="220" bordered class="member-sider">
          <n-menu
            v-model:value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
        </n-layout-sider>

        <!-- 右側：訂單列表 -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <header class="page-header">
            <p>訂單紀錄</p>
          </header>

          <!-- 載入中 -->
          <div v-if="loading" class="center-box">
            <n-spin size="large" />
          </div>

          <!-- 沒有訂單 -->
          <n-result
            v-else-if="orders.length === 0"
            status="info"
            title="尚無訂單紀錄"
            description="您目前還沒有任何訂單，快去購物吧！"
          >
            <template #footer>
              <n-button type="primary" round @click="$router.push('/shop/popular')">前往購物</n-button>
            </template>
          </n-result>

          <!-- 訂單列表 -->
          <template v-else>
            <n-card
              v-for="order in orders"
              :key="order.orderNo"
              class="order-card"
              size="small"
            >
              <!-- 訂單標頭 -->
              <div class="order-head">
                <div class="order-meta">
                  <span class="order-no">{{ order.orderNo }}</span>
                  <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                </div>
                <n-tag :type="statusType(order.status)" size="small" round>
                  {{ statusLabel(order.status) }}
                </n-tag>
              </div>

              <!-- 商品列表 -->
              <div class="items-list">
                <div
                  v-for="item in order.items"
                  :key="item.orderItemId"
                  class="order-item"
                >
                  <img
                    v-if="item.productImage"
                    :src="item.productImage"
                    :alt="item.productName"
                    class="item-img"
                  />
                  <div class="item-info">
                    <span class="item-name">{{ item.productName }}</span>
                    <span class="item-size">{{ item.sizeName }}</span>
                  </div>
                  <div class="item-right">
                    <span class="item-qty">x{{ item.quantity }}</span>
                    <span class="item-price">NT $ {{ item.subtotal.toLocaleString() }}</span>
                  </div>
                </div>
              </div>

              <!-- 訂單底部 -->
              <div class="order-foot">
                <div class="order-shipping">
                  物流：{{ shippingLabel(order.shippingMethod) }}
                  ／付款：{{ paymentLabel(order.paymentMethod) }}
                </div>
                <div class="order-right">
                  <div class="order-total">
                    合計
                    <span class="total-price">NT $ {{ order.totalAmount.toLocaleString() }}</span>
                  </div>
                  <!-- 操作按鈕 -->
                  <div class="action-btns">
                    <n-button
                      v-if="canRepay(order.status)"
                      type="primary"
                      size="small"
                      round
                      :loading="repayingOrders.has(order.orderNo)"
                      @click="handleRepay(order.orderNo)"
                    >
                      前往付款
                    </n-button>
                    <n-button
                      v-if="canCancel(order.status)"
                      size="small"
                      round
                      :loading="cancellingOrders.has(order.orderNo)"
                      @click="handleCancel(order)"
                    >
                      取消訂單
                    </n-button>
                    <n-button
                      size="small"
                      round
                      class="qa-btn"
                      @click="openQaModal(order.orderNo)"
                    >
                      <template #icon>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/>
                        </svg>
                      </template>
                      訂單問答
                    </n-button>
                  </div>
                </div>
              </div>
            </n-card>
          </template>
        </n-layout-content>
      </n-layout>
    </n-layout>

    <!-- 訂單問答 Modal -->
    <n-modal
      v-model:show="qaModalVisible"
      preset="card"
      :title="`訂單問答 — ${qaOrderNo}`"
      :style="{ width: '560px', maxWidth: '94vw' }"
      :segmented="{ content: true, footer: true }"
    >
      <!-- 既有問答列表 -->
      <div class="qa-list">
        <div v-if="qaLoading" class="qa-empty">載入中...</div>
        <template v-else-if="qaItems.length > 0">
          <div v-for="item in qaItems" :key="item.qaId" class="qa-item">
            <div class="qa-q">
              <span class="qa-badge qa-badge--q">Q</span>
              <div class="qa-body">
                <p class="qa-text">{{ item.question }}</p>
                <span class="qa-meta">{{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
            <div class="qa-a">
              <span class="qa-badge qa-badge--a">A</span>
              <div class="qa-body">
                <p v-if="item.answer" class="qa-text">{{ item.answer }}</p>
                <p v-else class="qa-text qa-text--pending">客服尚未回覆，請耐心等候...</p>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="qa-empty">目前尚無問答，歡迎提問！</div>
      </div>

      <!-- 提問區 -->
      <template #footer>
        <div class="qa-form">
          <p class="qa-form-label">提問</p>
          <n-input
            v-model:value="qaQuestion"
            type="textarea"
            placeholder="請輸入您關於此訂單的問題，客服將盡快回覆"
            :rows="3"
            :maxlength="500"
            show-count
          />
          <div class="qa-form-actions">
            <n-button @click="qaModalVisible = false">關閉</n-button>
            <n-button
              type="primary"
              :loading="qaSubmitting"
              :disabled="!qaQuestion.trim()"
              @click="submitOrderQuestion"
            >
              送出提問
            </n-button>
          </div>
        </div>
      </template>
    </n-modal>

    <!-- 取消確認 dialog -->
    <n-modal v-model:show="showCancelModal" preset="dialog" type="warning"
      title="確認取消訂單"
      positive-text="確認取消"
      negative-text="返回"
      @positive-click="confirmCancel"
    >
      <p>確定要取消訂單 <strong>{{ cancelTarget?.orderNo }}</strong> 嗎？此操作無法復原。</p>
    </n-modal>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuOption } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { orderService, type Order } from '@/services/order'
import { qaService, type QaItem } from '@/services/qa'

const router = useRouter()
const route = useRoute()
const message = useMessage()

/* ── ECPay 表單容器 ── */
const ecpayFormContainer = ref<HTMLElement | null>(null)

/* ── 左側選單 ── */
const activeKey = ref<string>('orders')

const getMenuKeyByPath = (path: string) => {
  if (path.startsWith('/member/orders')) return 'orders'
  if (path.startsWith('/member/wishlist') || path.startsWith('/member/favorite')) return 'favorite'
  if (path.startsWith('/member/qa')) return 'qa'
  if (path.startsWith('/member/profile')) return 'profile'
  return 'dashboard'
}

watch(() => route.path, (p) => { activeKey.value = getMenuKeyByPath(p) }, { immediate: true })

const menuOptions: MenuOption[] = [
  { key: 'dashboard', label: '會員中心' },
  { key: 'favorite', label: '我的收藏' },
  { key: 'orders', label: '訂單紀錄' },
  { key: 'qa', label: '商品問答紀錄' },
  { key: 'profile', label: '修改會員資料與密碼' },
]

const handleMenuSelect = (key: string) => {
  const pathMap: Record<string, string> = {
    dashboard: '/member',
    favorite: '/member/wishlist',
    orders: '/member/orders',
    qa: '/member/qa',
    profile: '/member/profile',
  }
  router.push(pathMap[key] ?? '/member')
}

/* ── 訂單資料 ── */
const loading = ref(true)
const orders = ref<Order[]>([])

onMounted(async () => {
  try {
    orders.value = await orderService.getMemberOrders()
  } catch (err: any) {
    message.error(err.message || '載入訂單失敗')
  } finally {
    loading.value = false
  }
})

/* ── 操作狀態 ── */
const canRepay = (status: string) => status === 'pending'
const canCancel = (status: string) => status === 'pending' || status === 'FAILED'

/* ── 前往付款 ── */
const repayingOrders = ref(new Set<string>())

const handleRepay = async (orderNo: string) => {
  repayingOrders.value.add(orderNo)
  try {
    const res = await orderService.repayOrder(orderNo)
    if (res?.paymentUrl && res?.ecpayParams) {
      localStorage.setItem('lastOrderNo', orderNo)
      submitECPayForm(res.paymentUrl, res.ecpayParams)
    }
  } catch (err: any) {
    message.error(err.message || '取得付款資訊失敗')
    repayingOrders.value.delete(orderNo)
  }
}

const submitECPayForm = (actionUrl: string, params: Record<string, string>) => {
  if (!ecpayFormContainer.value) return
  const form = document.createElement('form')
  form.method = 'POST'
  form.action = actionUrl
  for (const key in params) {
    if (Object.prototype.hasOwnProperty.call(params, key)) {
      const input = document.createElement('input')
      input.type = 'hidden'
      input.name = key
      input.value = params[key]
      form.appendChild(input)
    }
  }
  ecpayFormContainer.value.appendChild(form)
  form.submit()
}

/* ── 取消訂單 ── */
const cancellingOrders = ref(new Set<string>())
const showCancelModal = ref(false)
const cancelTarget = ref<Order | null>(null)

const handleCancel = (order: Order) => {
  cancelTarget.value = order
  showCancelModal.value = true
}

const confirmCancel = async () => {
  const order = cancelTarget.value
  if (!order) return
  cancellingOrders.value.add(order.orderNo)
  try {
    await orderService.cancelOrder(order.orderNo)
    order.status = 'CANCELLED'
    message.success('訂單已取消')
  } catch (err: any) {
    message.error(err.message || '取消訂單失敗')
  } finally {
    cancellingOrders.value.delete(order.orderNo)
    cancelTarget.value = null
  }
}

/* ── 訂單問答 ── */
const qaModalVisible = ref(false)
const qaOrderNo = ref('')
const qaItems = ref<QaItem[]>([])
const qaLoading = ref(false)
const qaQuestion = ref('')
const qaSubmitting = ref(false)

const openQaModal = async (orderNo: string) => {
  qaOrderNo.value = orderNo
  qaItems.value = []
  qaQuestion.value = ''
  qaModalVisible.value = true
  qaLoading.value = true
  try {
    qaItems.value = await qaService.getOrderQa(orderNo)
  } catch {
    // silent
  } finally {
    qaLoading.value = false
  }
}

const submitOrderQuestion = async () => {
  if (!qaQuestion.value.trim() || qaSubmitting.value) return
  qaSubmitting.value = true
  try {
    const newQa = await qaService.addOrderQuestion(qaOrderNo.value, qaQuestion.value.trim())
    qaItems.value.unshift(newQa)
    qaQuestion.value = ''
    message.success('提問已送出，客服將盡快回覆！')
  } catch (err: any) {
    message.error(err.message || '提問失敗，請稍後再試')
  } finally {
    qaSubmitting.value = false
  }
}

/* ── 標籤轉換 ── */
const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待付款',
    PAID: '已付款',
    FAILED: '付款失敗',
    SHIPPED: '已出貨',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] ?? status
}

const statusType = (status: string): 'default' | 'success' | 'warning' | 'error' | 'info' => {
  const map: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
    pending: 'warning',
    PAID: 'success',
    FAILED: 'error',
    SHIPPED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'default',
  }
  return map[status] ?? 'default'
}

const shippingLabel = (m: string) =>
  ({ home: '宅配到府', store: '超商取貨' }[m] ?? m)

const paymentLabel = (m: string) =>
  ({ ecpay: '綠界金流', credit_card: '信用卡' }[m] ?? m)

const formatDate = (d: string) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

<style scoped lang="scss">
.member-page {
  padding: 40px 0 80px;
  display: flex;
  justify-content: center;
}

.member-layout {
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  background-color: #ffffff;
}

.member-sider {
  padding: 16px 12px;
  background-color: #fff;
}

.page-header p {
  text-align: center;
  margin-bottom: 24px;
  font-size: 25px;
  font-weight: bold;
}

.center-box {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

/* ── 訂單卡片 ── */
.order-card {
  margin-bottom: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-weight: 600;
  font-size: 14px;
  font-family: monospace;
  letter-spacing: 0.03em;
  color: #353535;
}

.order-date {
  font-size: 12px;
  color: #999;
}

/* ── 商品行 ── */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
  background: #f5f5f5;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  font-size: 13px;
  color: #353535;
  font-weight: 500;
}

.item-size {
  font-size: 12px;
  color: #999;
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.item-qty {
  font-size: 12px;
  color: #aaa;
}

.item-price {
  font-size: 13px;
  font-weight: 500;
  color: #353535;
}

/* ── 底部 ── */
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.order-shipping {
  font-size: 12px;
  color: #aaa;
}

.order-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.order-total {
  font-size: 13px;
  color: #888;
}

.total-price {
  font-weight: 700;
  font-size: 16px;
  color: #353535;
  margin-left: 6px;
}

.action-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.qa-btn {
  border-color: #8A897C;
  color: #8A897C;

  &:hover {
    background: #f0eeeb;
  }
}

/* ── Q&A Modal ── */
.qa-list {
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-size: 13px;
  padding-right: 2px;
}

.qa-empty {
  color: #aaa;
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
}

.qa-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f2f2f2;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.qa-q, .qa-a {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.qa-badge {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;

  &--q { background: #353535; color: #fff; }
  &--a { background: #eef5f0; color: #4a8c5c; }
}

.qa-body { flex: 1; }

.qa-text {
  margin: 0 0 2px;
  line-height: 1.5;
  color: #353535;

  &--pending { color: #bbb; font-style: italic; }
}

.qa-meta { font-size: 11px; color: #ccc; }

.qa-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.qa-form-label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
  margin: 0;
}

.qa-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 768px) {
  .member-page { padding: 16px 0; }
  .member-layout { margin: 0 8px; }
  .order-head { flex-wrap: wrap; gap: 8px; }
  .order-foot { flex-direction: column; align-items: flex-start; }
  .order-right { align-items: flex-start; }
}
</style>
