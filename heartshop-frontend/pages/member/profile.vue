<template>
  <main class="member-page">
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

        <!-- 右側：會員資料 + 修改密碼 -->
        <n-layout-content content-style="padding: 24px 32px 40px;">
          <!-- 個人資料標題 -->
          <header class="profile-header">
            <p class="profile-zh">個人資料修改</p>
          </header>

          <!-- 個人資料表單 -->
          <section class="profile-section">
            <form class="profile-form" @submit.prevent="onSubmitProfile">
              <!-- 會員帳號（不可編輯示意） -->
              <div class="form-row">
                <label class="form-label">會員帳號</label>
                <div class="form-field">
                  <input type="email" v-model="profileForm.account" disabled />
                </div>
              </div>

              <!-- 會員姓名 -->
              <div class="form-row">
                <label class="form-label">會員姓名</label>
                <div class="form-field">
                  <input
                    type="text"
                    v-model="profileForm.name"
                    placeholder="請輸入姓名"
                  />
                </div>
              </div>

              <!-- 手機號碼 -->
              <div class="form-row">
                <label class="form-label">手機號碼</label>
                <div class="form-field">
                  <input
                    type="tel"
                    v-model="profileForm.phone"
                    placeholder="請輸入手機號碼"
                  />
                </div>
              </div>

              <!-- 生日 -->
              <div class="form-row">
                <label class="form-label">會員生日</label>
                <div class="form-field">
                  <input type="date" v-model="profileForm.birthday" />
                </div>
              </div>

              <!-- 聯絡地址：改成單一欄位 -->
              <div class="form-row">
                <label class="form-label">聯絡地址</label>
                <div class="form-field">
                  <input
                    type="text"
                    v-model="profileForm.address"
                    placeholder="請輸入詳細地址"
                  />
                </div>
              </div>

              <!-- EDM 同意 -->
              <div class="form-row">
                <label class="form-label">訂閱電子報</label>
                <div class="form-field">
                  <label class="checkbox-label">
                    <input
                      type="radio"
                      value="yes"
                      v-model="profileForm.subscribeEdm"
                    />
                    同意訂閱電子報
                  </label>
                  <label class="checkbox-label">
                    <input
                      type="radio"
                      value="no"
                      v-model="profileForm.subscribeEdm"
                    />
                    取消訂閱電子報
                  </label>
                </div>
              </div>

              <!-- 送出按鈕 -->
              <div class="form-actions">
                <button type="submit" class="primary-btn">修改送出</button>
              </div>
            </form>
          </section>

          <!-- 修改密碼標題 -->
          <section class="password-section">
            <header class="profile-header">
              <p class="profile-zh">修改密碼</p>
            </header>

            <!-- 修改密碼表單：跟上面同一寬度 -->
            <form class="profile-form" @submit.prevent="onSubmitPassword">
              <div class="form-row">
                <label class="form-label">舊密碼</label>
                <div class="form-field">
                  <input
                    type="password"
                    v-model="passwordForm.oldPassword"
                    placeholder="請輸入舊密碼"
                  />
                </div>
              </div>

              <div class="form-row">
                <label class="form-label">新密碼</label>
                <div class="form-field">
                  <input
                    type="password"
                    v-model="passwordForm.newPassword"
                    placeholder="8-16碼英數字"
                  />
                </div>
              </div>

              <div class="form-row">
                <label class="form-label">確認新密碼</label>
                <div class="form-field">
                  <input
                    type="password"
                    v-model="passwordForm.confirmPassword"
                    placeholder="再輸入一次新密碼"
                  />
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="primary-btn">修改送出</button>
              </div>
            </form>
          </section>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { MenuOption, FormInst, FormRules } from "naive-ui";
import { useMessage } from "naive-ui";
import { useRouter } from "vue-router";

const router = useRouter();
const message = useMessage();
const activeKey = ref<string | null>("profile");

// ✅ 讀取 cookies
const token = useCookie<string | null>("token");
const memberInfoCookie = useCookie<{
  memberId: number;
  email: string;
  name: string;
} | null>("memberInfo");

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

// ✅ 從 cookie 初始化表單資料
const profileForm = ref({
  account: memberInfoCookie.value?.email || "", // 會員帳號（不可編輯）
  name: memberInfoCookie.value?.name || "", // 會員姓名
  phone: "", // 手機號碼
  birthday: "", // 生日
  address: "", // 地址
  subscribeEdm: "no",
});

const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// ✅ Form ref 和驗證規則
const profileFormRef = ref<FormInst | null>(null);
const passwordFormRef = ref<FormInst | null>(null);

const profileRules: FormRules = {
  name: [
    { required: true, message: "請輸入姓名", trigger: "blur" },
    {
      min: 2,
      max: 100,
      message: "名稱長度需在 2-100 字元之間",
      trigger: "blur",
    },
  ],
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: "請輸入舊密碼", trigger: "blur" }],
  newPassword: [
    { required: true, message: "請輸入新密碼", trigger: "blur" },
    { min: 6, max: 20, message: "密碼長度需在 6-20 字元之間", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "請再次輸入新密碼", trigger: "blur" },
    {
      validator: (_rule, value) => value === passwordForm.value.newPassword,
      message: "兩次密碼輸入不一致",
      trigger: ["blur", "input"],
    },
  ],
};

// ✅ 在客戶端檢查登入狀態
onMounted(() => {
  if (!memberInfoCookie.value) {
    router.push("/");
  }
});

// ==================== API 呼叫 ====================

interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp: string;
}

interface MemberDTO {
  memberId: number;
  account: string;
  email: string;
  name: string;
  createdAt: string;
}

// API: 更新會員資料
async function updateProfileApi(data: {
  name: string;
  phone?: string;
  birthday?: string;
  address?: string;
}) {
  const response = await $fetch<ApiResponse<MemberDTO>>(
    "/api/members/profile",
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token.value}`,
      },
      body: data,
    }
  );
  return response.data;
}

// API: 修改密碼
async function changePasswordApi(data: {
  oldPassword: string;
  newPassword: string;
}) {
  await $fetch("/api/members/password", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token.value}`,
    },
    body: data,
  });
}

// ==================== 提交處理 ====================

const onSubmitProfile = async () => {
  await profileFormRef.value?.validate();

  try {
    const updatedMember = await updateProfileApi({
      name: profileForm.value.name,
      phone: profileForm.value.phone || undefined,
      birthday: profileForm.value.birthday || undefined,
      address: profileForm.value.address || undefined,
    });

    // 更新 memberInfo cookie（同步名稱變更）
    memberInfoCookie.value = {
      memberId: updatedMember.memberId,
      email: updatedMember.email,
      name: updatedMember.name,
    };

    message.success("個人資料更新成功");
  } catch (e: any) {
    console.error(e);
    const errorMsg = e?.data?.message || e?.message || "更新失敗，請稍後再試";
    message.error(errorMsg);
  }
};

const onSubmitPassword = async () => {
  await passwordFormRef.value?.validate();

  try {
    await changePasswordApi({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });

    // 清空表單
    passwordForm.value = {
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    };

    message.success("密碼修改成功");
  } catch (e: any) {
    console.error(e);
    const errorMsg = e?.data?.message || e?.message || "密碼修改失敗";
    message.error(errorMsg);
  }
};
</script>

<style scoped lang="scss">
/* ★ 不再限制 member-page 的寬度，只當上下留白用 */
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
.profile-header {
  text-align: center;
  margin-bottom: 24px;

  .profile-zh {
    font-size: 25px;
    font-weight: bold;
  }
}

/* 個人資料 / 密碼 區塊共用 */
.profile-section,
.password-section {
  margin-bottom: 40px;
}

/* 控制表單整體寬度：跟截圖差不多寬 */
.profile-form {
  max-width: 720px;
  margin: 0 auto;
}

/* 每一列：左 label 右 input */
.form-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  align-items: center;
  column-gap: 12px;
  margin-bottom: 12px;

  .form-label {
    font-size: 13px;
  }

  .form-field {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    input,
    select {
      flex: 1 1 auto;
      min-width: 0;
      padding: 6px 8px;
      font-size: 13px;
      border: 1px solid #ccc;
      outline: none;

      &:disabled {
        background-color: #f5f5f5;
        color: #353535;
      }
    }
  }
}

/* EDM radio */
.checkbox-label {
  font-size: 13px;
  margin-right: 16px;

  input {
    margin-right: 4px;
  }
}

/* 送出鈕 */
.form-actions {
  text-align: center;
  margin-top: 20px;

  .primary-btn {
    min-width: 180px;
    padding: 8px 16px;
    border: none;
    background-color: #222;
    color: #fff;
    font-size: 13px;
    cursor: pointer;
  }
}

/* RWD */
@media (max-width: 768px) {
  .member-page {
    padding: 16px 0;
  }

  .profile-form {
    max-width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
    align-items: flex-start;

    .form-label {
      margin-bottom: 4px;
    }
  }
}
</style>
