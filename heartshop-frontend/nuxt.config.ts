import { loadEnv } from "vite";
import { resolve } from "path";
import { fileURLToPath } from "url";

// 取得根目錄路徑（heartshop-frontend 的上層）
const rootDir = resolve(fileURLToPath(import.meta.url), "../../");

// 從根目錄載入環境變數（涵蓋所有前綴，包含 DB_* 與 VITE_*）
const env = loadEnv("", rootDir, "");
const apiHost = env.VITE_API_BASE_URL || "http://localhost:8080";

const adminUrl = env.VITE_ADMIN_URL || "http://localhost:5173";

export default defineNuxtConfig({
  compatibilityDate: "2025-07-15",

  runtimeConfig: {
    public: {
      adminUrl,
    },
  },

  devtools: {
    enabled: true,
  },

  modules: [
    "@pinia/nuxt"
  ],

  build: {
    transpile: [
      "naive-ui",
      "vueuc",
      "@css-render/vue3-ssr",
      "@juggle/resize-observer",
    ],
  },

  vite: {
    // 讓 Vite 從根目錄讀取 .env，供元件內 import.meta.env.VITE_* 使用
    envDir: rootDir,
    server: {
      watch: {
        usePolling: true,
      },
    },
    ssr: {
      noExternal: [
        "naive-ui",
        "vueuc",
        "@css-render/vue3-ssr",
        "@juggle/resize-observer",
      ],
    },
  },

  nitro: {
    devProxy: {
      "/api": {
        target: `${apiHost}/api`,
        changeOrigin: true,
        // prependPath: true, // 注意：Nuxt 4/Nitro 有時不需要這個，若代理失敗可嘗試註解掉
      },
      "/uploads": {
        target: `${apiHost}/api/uploads`,
        changeOrigin: true,
      },
    },
  },
});
