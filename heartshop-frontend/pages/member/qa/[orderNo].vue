<!-- pages/member/qa/[orderNo].vue -->
<template>
  <main class="member-page">
    <n-layout class="member-layout">
      <n-layout has-sider>
        <!-- 左側選單：跟其他會員頁一樣 -->
        <n-layout-sider width="220" bordered class="member-sider">
          <n-menu
            v-model:value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
        </n-layout-sider>

        <!-- 右側：訂單 Q&A -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <!-- 標題 -->
          <header class="qa-header">
            <p class="qa-zh">訂單問與答</p>
          </header>

          <p class="greeting">
            Hi {{ memberName }}，這是訂單
            <span class="highlight">{{ orderNo }}</span>
            的問答區。
          </p>

          <!-- 提問表單 -->
          <section class="qa-form">
            <h2 class="block-title">我要提問</h2>
            <textarea
              v-model="newQuestion"
              class="qa-textarea"
              placeholder="請輸入您的問題"
            />
            <div class="qa-form-footer">
              <!-- 這裡你可以照原站加驗證碼、提示文字等等 -->
              <n-button
                type="primary"
                :loading="submitting"
                @click="submitQuestion"
              >
                送出
              </n-button>
            </div>
          </section>

          <!-- 提問紀錄 -->
          <section class="qa-record">
            <h2 class="block-title">提問紀錄</h2>

            <!-- 載入中 -->
            <p v-if="loadingQa">載入中...</p>

            <!-- 如果沒有任何紀錄 -->
            <p v-else-if="qaList.length === 0" class="qa-empty">尚未提問</p>

            <!-- 有紀錄 -->
            <ul v-else class="qa-list">
              <li v-for="item in qaList" :key="item.id" class="qa-item">
                <p class="qa-question">問：{{ item.question }}</p>
                <p class="qa-meta">時間：{{ formatDate(item.createdAt) }}</p>
                <p v-if="item.answer" class="qa-answer">
                  答：{{ item.answer }}
                </p>
                <p v-else class="qa-pending">答：尚未回覆</p>
              </li>
            </ul>
          </section>

          <!-- 底部返回按鈕 -->
          <footer class="qa-footer">
            <button type="button" class="btn" @click="backToOrder">
              返回訂單明細
            </button>
          </footer>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { MenuOption } from "naive-ui";
import { useRouter, useRoute } from "vue-router";

interface QaItem {
  id: number;
  question: string;
  answer: string | null;
  createdAt: string;
}

const router = useRouter();
const route = useRoute();

const memberName = ref("黃風睿");

// 從網址拿到訂單編號： /member/qa/123456
const orderNo = computed(() => route.params.orderNo as string);

// 左側選單（跟其他頁一樣）
const activeKey = ref<string | null>("qa");

const menuOptions: MenuOption[] = [
  { key: "dashboard", label: "會員中心" },
  { key: "favorite", label: "我的收藏" },
  { key: "orders", label: "訂單紀錄" },
  { key: "qa", label: "商品問答紀錄" },
  { key: "profile", label: "修改會員資料與密碼" },
];

const handleMenuSelect = (key: string) => {
  activeKey.value = key;
  switch (key) {
    case "dashboard":
      router.push("/member");
      break;
    case "favorite":
      router.push("/member/wishlist");
      break;
    case "orders":
      router.push("/member/orders");
      break;
    case "qa":
      router.push("/member/qa");
      break;
    case "profile":
      router.push("/member/profile");
      break;
  }
};

// Q&A 狀態
const qaList = ref<QaItem[]>([]);
const loadingQa = ref(true);
const newQuestion = ref("");
const submitting = ref(false);

/** 取得這筆訂單的 Q&A 列表 */
const fetchOrderQa = async () => {
  loadingQa.value = true;
  try {
    const res = await fetch(`/api/member/orders/${orderNo.value}/qa`, {
      method: "GET",
    });

    if (!res.ok) {
      throw new Error("HTTP error " + res.status);
    }

    // 這裡才把 Response 轉成我們的 QaItem[]
    const data = (await res.json()) as QaItem[];
    qaList.value = data ?? [];
  } catch (e) {
    console.error("取得 Q&A 失敗", e);
    qaList.value = [];
  } finally {
    loadingQa.value = false;
  }
};

/** 送出新提問 */
const submitQuestion = async () => {
  if (!newQuestion.value.trim()) return;

  submitting.value = true;
  try {
    const res = await fetch(`/api/member/orders/${orderNo.value}/qa`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        question: newQuestion.value.trim(),
      }),
    });

    if (!res.ok) {
      throw new Error("HTTP error " + res.status);
    }

    newQuestion.value = "";
    await fetchOrderQa(); // 重新載入紀錄
  } catch (e) {
    console.error("送出提問失敗", e);
  } finally {
    submitting.value = false;
  }
};

const formatDate = (iso: string) => {
  const d = new Date(iso);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
};

const backToOrder = () => {
  router.push("/member/orders");
};

onMounted(() => {
  fetchOrderQa();
});
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

/* 標題 */
.qa-header {
  text-align: center;
  margin-bottom: 24px;
}
.qa-zh {
  font-size: 25px;
  font-weight: bold;
}

.greeting {
  font-size: 13px;
  margin-bottom: 12px;

  .highlight {
    font-weight: 600;
  }
}

.block-title {
  font-size: 14px;
  margin-bottom: 8px;
}

/* 提問區 */
.qa-form {
  border-top: 1px solid #ccc;
  border-bottom: 1px solid #eee;
  padding: 12px 0;
  margin-bottom: 24px;

  .qa-textarea {
    width: 95%;
    min-height: 140px;
    padding: 8px;
    font-size: 13px;
    border: 1px solid #ccc;
    resize: vertical;
  }

  .qa-form-footer {
    margin-top: 8px;
    display: flex;
    justify-content: flex-end;
    width: 95%;
  }
}

/* 紀錄區 */
.qa-record {
  border-top: 1px solid #ccc;
  padding-top: 12px;
}

.qa-empty {
  font-size: 13px;
  color: #353535;
}

.qa-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.qa-item {
  padding: 8px 10px;
  border: 1px solid #eee;
  background: #fafafa;
  font-size: 13px;
}

.qa-meta {
  font-size: 12px;
  color: #353535;
}

.qa-answer {
  margin-top: 4px;
  color: #353535;
}

.qa-pending {
  margin-top: 4px;
  color: #353535;
}

/* 底部按鈕 */
.qa-footer {
  margin-top: 24px;
  display: flex;
  justify-content: center;

  .btn {
    min-width: 160px;
    padding: 8px 16px;
    border: 1px solid #333;
    background: #fff;
    font-size: 13px;
    cursor: pointer;
  }
}
</style>
