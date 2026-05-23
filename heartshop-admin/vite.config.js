import { fileURLToPath, URL } from "node:url";
import { defineConfig, loadEnv } from "vite";
import path from "path";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { NaiveUiResolver } from "unplugin-vue-components/resolvers";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 從根目錄（admin 的上層）載入環境變數
  const rootDir = path.resolve(__dirname, "..");
  const env = loadEnv(mode, rootDir, "");
  const apiHost = env.VITE_API_BASE_URL || "http://localhost:8080";

  return {
    plugins: [
      vue(),
      AutoImport({
        imports: [
          "vue",
          "vue-router",
          "pinia", // 如果您有用 pinia，也加上這個
          {
            "naive-ui": [
              "useDialog",
              "useMessage",
              "useNotification",
              "useLoadingBar",
            ],
          },
        ],
        // 生成自動引入的 TS 聲明文件
        dts: "src/auto-imports.d.ts",
      }),
      Components({
        resolvers: [NaiveUiResolver()],
      }),
    ],

    // 讓 Vite 從根目錄讀取 .env（供 import.meta.env.VITE_* 使用）
    envDir: rootDir,

    server: {
      proxy: {
        "/api": {
          target: apiHost,
          changeOrigin: true,
        },
        "/uploads": {
          target: apiHost,
          changeOrigin: true,
        },
      },
    },
    resolve: {
      alias: {
        // 設置 @ 指向 src 目錄
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
  };
});
