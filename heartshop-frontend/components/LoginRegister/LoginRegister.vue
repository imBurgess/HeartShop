<!-- use modal、form、tabs -->
<template>
  <n-modal
    :show="show"
    preset="card"
    title="會員登入"
    :mask-closable="false"
    style="max-width: 480px"
    @update:show="onUpdateShow"
  >
    <n-tabs
      v-model:value="activeTab"
      type="line"
      justify-content="space-evenly"
    >
      <!--登入-->
      <n-tab-pane name="login" tab="登入">
        <n-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-placement="top"
        >
          <n-form-item label="Email" path="email">
            <n-input
              v-model:value="loginForm.email"
              placeholder="請輸入 Email"
              type="email"
            />
          </n-form-item>

          <n-form-item label="密碼" path="password">
            <n-input
              v-model:value="loginForm.password"
              type="password"
              placeholder="請輸入密碼"
              show-password-on="click"
            />
          </n-form-item>

          <div class="login-actions">
            <n-button quaternary @click="onUpdateShow(false)"> 取消 </n-button>
            <n-button color="#353535" @click="handleLoginSubmit">
              登入
            </n-button>
          </div>
        </n-form>
      </n-tab-pane>
      <!--註冊-->
      <n-tab-pane name="register" tab="註冊">
        <n-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-placement="top"
        >
          <n-form-item label="Email" path="email">
            <n-input
              v-model:value="registerForm.email"
              placeholder="請輸入 Email"
              type="email"
            ></n-input>
          </n-form-item>

          <n-form-item label="顯示名稱" path="name">
            <n-input
              v-model:value="registerForm.name"
              placeholder="請輸入名稱"
            ></n-input>
          </n-form-item>

          <n-form-item label="密碼" path="password">
            <n-input
              v-model:value="registerForm.password"
              placeholder="請輸入密碼"
              type="password"
              show-password-on="click"
            ></n-input>
          </n-form-item>

          <n-form-item label="確認密碼" path="confirmPassword">
            <n-input
              v-model:value="registerForm.confirmPassword"
              placeholder="請再次輸入密碼"
              type="password"
              show-password-on="click"
            ></n-input>
          </n-form-item>

          <div class="login-actions">
            <n-button quaternary @click="onUpdateShow(false)">取消</n-button>
            <n-button color="#353535" @click="handleRegisterSubmit"
              >註冊</n-button
            >
          </div>
        </n-form>
      </n-tab-pane>
    </n-tabs>
  </n-modal>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { FormInst, FormRules } from "naive-ui";
import { useMessage } from "naive-ui";

// 父元件用 v-model:show 傳進來
const props = withDefaults(defineProps<{
  show?: boolean;
}>(), {
  show: false
});

// 跟父元件溝通
const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "login-success"): void;
  (e: "register-success"): void;
}>();

const message = useMessage();

// ✅ 用 cookie 存 token（Navbar 也用同一把 key 才會同步）
const token = useCookie<string | null>("token", {
  sameSite: "lax",
  // secure: true, // 若你是 https 再打開
});

// ✅ 用 cookie 存會員資訊（包含 name、email 等）
const memberInfo = useCookie<{
  memberId: number;
  email: string;
  name: string;
  role?: string;
} | null>("memberInfo", {
  sameSite: "lax",
});

const activeTab = ref<"login" | "register">("login");

const loginFormRef = ref<FormInst | null>(null);
const loginForm = ref({
  email: "",
  password: "",
});
const loginRules: FormRules = {
  email: [
    { required: true, message: "請輸入 Email", trigger: "blur" },
    { type: "email", message: "Email 格式不正確", trigger: ["blur", "input"] },
  ],
  password: [{ required: true, message: "請輸入密碼", trigger: "blur" }],
};

const registerFormRef = ref<FormInst | null>(null);
const registerForm = ref({
  email: "",
  name: "",
  password: "",
  confirmPassword: "",
});
const registerRules: FormRules = {
  email: [
    { required: true, message: "請輸入 Email", trigger: "blur" },
    { type: "email", message: "Email 格式不正確", trigger: "blur" },
  ],
  name: [
    { required: true, message: "請輸入顯示名稱", trigger: "blur" },
    {
      min: 2,
      max: 100,
      message: "名稱長度需在 2-100 字元之間",
      trigger: ["blur", "input"],
    },
  ],
  password: [
    { required: true, message: "請輸入密碼", trigger: "blur" },
    {
      min: 6,
      max: 20,
      message: "密碼長度需在 6-20 字元之間",
      trigger: ["blur", "input"],
    },
  ],
  confirmPassword: [
    { required: true, message: "請再次輸入密碼", trigger: "blur" },
    {
      validator: (_rule, value) => value === registerForm.value.password,
      message: "兩次輸入的密碼不一致",
      trigger: ["blur", "input"],
    },
  ],
};

const onUpdateShow = (val: boolean) => {
  emit("update:show", val);
};

// ==================== API 定義 ====================

// 定義後端回傳格式
interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp: string;
}

// 登入成功回傳的資料結構
interface LoginResponseData {
  token: string;
  member: {
    memberId: number;
    account: string;
    email: string;
    name: string;
    createdAt: string;
  };
}

// 註冊成功回傳的資料結構
interface MemberDTO {
  memberId: number;
  account: string;
  email: string;
  name: string;
  createdAt: string;
}

// ✅ 真實登入 API：呼叫後端 /api/members/login
async function loginApi(
  email: string,
  password: string,
): Promise<LoginResponseData> {
  try {
    const response = await $fetch<ApiResponse<LoginResponseData>>(
      "/api/members/login",
      {
        method: "POST",
        body: { email, password },
      },
    );

    // 檢查回傳的 token 和 member
    if (!response.data?.token || !response.data?.member) {
      throw new Error("登入失敗：未取得完整資料");
    }

    // 回傳完整的資料（包含 token 和 member）
    return response.data;
  } catch (error: any) {
    console.error("登入 API 錯誤：", error);

    // 處理後端回傳的錯誤訊息
    if (error.data?.message) {
      throw new Error(error.data.message);
    }

    throw new Error("登入失敗，請確認帳號密碼");
  }
}

// ✅ 真實註冊 API：呼叫後端 /api/members/register
async function registerApi(data: {
  email: string;
  name: string;
  password: string;
}): Promise<void> {
  try {
    const response = await $fetch<ApiResponse<MemberDTO>>(
      "/api/members/register",
      {
        method: "POST",
        body: data,
      },
    );

    // 可選：記錄註冊成功的會員資訊
    console.log("註冊成功：", response.data);
  } catch (error: any) {
    console.error("註冊 API 錯誤：", error);

    // 處理後端回傳的錯誤訊息
    if (error.data?.message) {
      throw new Error(error.data.message);
    }

    // 處理 Email 重複的情況（HTTP 409）
    if (error.status === 409 || error.statusCode === 409) {
      throw new Error("此 Email 已被註冊，請使用其他 Email");
    }

    throw new Error("註冊失敗，請稍後再試");
  }
}

const handleLoginSubmit = async () => {
  loginFormRef.value?.validate(async (errors) => {
    if (errors) return;

    try {
      const { email, password } = loginForm.value;

      // 1) 呼叫登入 API 拿 token 和會員資料
      const res = await loginApi(email, password);

      // 2) ✅ 存 token 到 cookie（Navbar 會立刻判斷為已登入）
      token.value = res.token;

      // 3) ✅ 存會員資訊到 cookie（會員中心會用到）
      memberInfo.value = {
        memberId: res.member.memberId,
        email: res.member.email,
        name: res.member.name,
        role: res.member.role,
      };

      // 4) 告訴父元件登入成功
      emit("login-success");

      // 5) 關閉視窗
      emit("update:show", false);

      message.success("登入成功");
    } catch (e: any) {
      console.error(e);
      const errorMsg = e?.message || "登入失敗，請確認帳號密碼";
      message.error(errorMsg);
    }
  });
};

const handleRegisterSubmit = async () => {
  registerFormRef.value?.validate(async (errors) => {
    if (errors) return;

    try {
      const { email, name, password } = registerForm.value;

      // 1) 註冊
      await registerApi({ email, name, password });

      message.success("註冊成功，請登入");
      emit("register-success");

      // 2) 註冊完成後切回登入頁
      activeTab.value = "login";
    } catch (e: any) {
      console.error(e);
      // 優先顯示後端的錯誤訊息，如果沒有則顯示通用訊息
      const errorMsg = e?.message || "註冊失敗，請稍後再試";
      message.error(errorMsg);
    }
  });
};
</script>

<style scoped>
.login-actions {
  display: flex;
  justify-content: flex-end; /* 讓裡面的按鈕靠右 */
  gap: 8px; /* 兩個按鈕之間留一點間距，可自行調整 */
  margin-top: 16px; /* 跟上面的表單內容拉開一點距離 */
}

/* 覆蓋 n-tabs 的預設綠色為 #BDBBB0 */
:deep(.n-tabs-tab:hover),
:deep(.n-tabs-tab:hover .n-tabs-tab__label) {
  color: #8a897c !important;
}
:deep(.n-tabs-tab--active),
:deep(.n-tabs-tab--active .n-tabs-tab__label) {
  color: #8a897c !important;
}
:deep(.n-tabs-bar) {
  background-color: #8a897c !important;
}
</style>
