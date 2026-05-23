<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左側裝飾區 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo-area">
            <svg
              class="logo-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                fill="currentColor"
              />
            </svg>
            <h1 class="logo-title">拾心市集</h1>
          </div>
          <p class="decoration-subtitle">優雅管理您的線上商店</p>
          <div class="decoration-features">
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none">
                <path
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              <span>簡潔直觀的介面</span>
            </div>
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M13 10V3L4 14h7v7l9-11h-7z" fill="currentColor" />
              </svg>
              <span>高效能管理工具</span>
            </div>
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none">
                <path
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              <span>安全可靠的系統</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側登入表單 -->
      <div class="login-form-area">
        <div class="login-card">
          <div class="login-header">
            <h2 class="login-title">歡迎回來</h2>
            <p class="login-subtitle">請登入您的帳號以繼續</p>
          </div>

          <form class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none">
                  <path
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    fill="currentColor"
                  />
                </svg>
                <span>帳號</span>
              </label>
              <input
                v-model.trim="form.account"
                type="text"
                placeholder="帳號或 Email"
                autocomplete="username"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none">
                  <path
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
                <span>密碼</span>
              </label>
              <input
                v-model.trim="form.password"
                type="password"
                placeholder="請輸入密碼"
                autocomplete="current-password"
                class="form-input"
              />
            </div>

            <p v-if="errorMsg" class="error-message">
              <svg viewBox="0 0 24 24" fill="none">
                <path
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              {{ errorMsg }}
            </p>

            <button class="login-button" type="submit" :disabled="loading">
              <span v-if="!loading">登入</span>
              <span v-else class="loading-spinner">
                <svg viewBox="0 0 24 24" fill="none">
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-opacity="0.3"
                  />
                  <path
                    d="M12 2a10 10 0 019.9 8.5"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-linecap="round"
                  />
                </svg>
                登入中...
              </span>
            </button>

          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(false);
const errorMsg = ref("");

const form = reactive({
  account: "",
  password: "",
});

async function handleLogin() {
  errorMsg.value = "";

  if (!form.account || !form.password) {
    errorMsg.value = "請輸入帳號與密碼";
    return;
  }

  loading.value = true;
  try {
    const res = await fetch("/api/members/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: form.account, password: form.password }),
    });

    const json = await res.json();

    if (!res.ok || json.code !== "0000") {
      errorMsg.value = json.message || "帳號或密碼錯誤";
      return;
    }

    const { token, member } = json.data;

    if (member.role !== "ADMIN") {
      errorMsg.value = "此帳號無管理員權限";
      return;
    }

    auth.login({
      token,
      user: { name: member.name, role: member.role },
    });

    const redirect = (route.query.redirect as string) || "/";
    router.replace(redirect);
  } catch {
    errorMsg.value = "網路錯誤，請稍後再試";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 大地暖色調漸層背景，與前台一致 */
  background: linear-gradient(135deg, #8A897C 0%, #5a5950 60%, #3d3c36 100%);
  padding: var(--spacing-md);
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: -50%;
    right: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.08) 0%,
      transparent 70%
    );
    animation: float 20s ease-in-out infinite;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(-50px, -50px);
  }
}

.login-container {
  width: 100%;
  max-width: 1000px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: slideInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.login-decoration {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-primary-dark) 100%
  );
  padding: var(--spacing-xl);
  color: var(--color-white);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: -100px;
    left: -100px;
    width: 300px;
    height: 300px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }

  &::after {
    content: "";
    position: absolute;
    bottom: -150px;
    right: -150px;
    width: 400px;
    height: 400px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 50%;
  }

  @media (max-width: 768px) {
    display: none;
  }
}

.decoration-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing);
}

.logo-icon {
  width: 48px;
  height: 48px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.logo-title {
  font-size: 2rem;
  font-weight: var(--font-weight-bold);
  margin: 0;
}

.decoration-subtitle {
  font-size: 1.125rem;
  opacity: 0.9;
  margin: 0 0 var(--spacing-xl);
}

.decoration-features {
  display: flex;
  flex-direction: column;
  gap: var(--spacing);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing);
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius);
  backdrop-filter: blur(10px);

  svg {
    width: 24px;
    height: 24px;
    flex-shrink: 0;
  }

  span {
    font-size: 0.9375rem;
    font-weight: var(--font-weight-medium);
  }
}

.login-form-area {
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: var(--spacing-lg);
  text-align: center;
}

.login-title {
  font-size: 1.75rem;
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0 0 var(--spacing-sm);
}

.login-subtitle {
  font-size: 0.9375rem;
  color: var(--color-gray-600);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);

  svg {
    width: 16px;
    height: 16px;
    color: var(--color-primary);
  }
}

.form-input {
  height: 48px;
  padding: 0 var(--spacing);
  font-size: 0.9375rem;
  color: var(--color-gray-900);
  background: var(--color-white);
  border: 2px solid var(--color-gray-300);
  border-radius: var(--radius);
  outline: none;
  transition: all var(--transition-base);

  &::placeholder {
    color: var(--color-gray-500);
  }

  &:hover {
    border-color: var(--color-gray-400);
  }

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px var(--color-primary-bg);
  }
}

.error-message {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing);
  font-size: 0.875rem;
  color: var(--color-error);
  background: rgba(244, 67, 54, 0.08);
  border-radius: var(--radius);
  margin: 0;

  svg {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }
}

.login-button {
  height: 48px;
  margin-top: var(--spacing-sm);
  font-size: 1rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-white);
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-primary-dark) 100%
  );
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-base);

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);

  svg {
    width: 20px;
    height: 20px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing);
  font-size: 0.8125rem;
  color: var(--color-gray-600);
  background: var(--color-gray-100);
  border-radius: var(--radius);
  text-align: center;

  svg {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }

  strong {
    color: var(--color-primary);
    font-weight: var(--font-weight-semibold);
  }
}
</style>
