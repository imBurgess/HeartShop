# HeartShop — AI 助理工作規範

本文件為協助 AI 助理（Gemini、Claude 等）在此專案進行程式碼生成、重構、除錯與維護時的最高參考指引。每次執行任務前請先閱讀本文件。

---

## 一、專案架構

```
heart-shop/
├── .env                        # 統一環境變數（所有子專案共用）
├── .env.example
├── heartshopsql/schema.sql     # 完整資料庫建表腳本
├── heartshop-frontend/         # 前端商城（Nuxt 3）
├── heartshop-admin/            # 管理後台（Vue 3 + Vite）
└── HeartShop-Spring/HeartShop/ # 後端（Spring Boot 3 + MyBatis）
```

**服務埠號**

| 服務 | URL |
|------|-----|
| 後端 API | `http://localhost:8080/api` |
| 前端商城 | `http://localhost:3000` |
| 管理後台 | `http://localhost:5173` |

---

## 二、技術棧

### 前端商城 (heartshop-frontend)

- Nuxt 3（`compatibilityDate: 2025-07-15`）、Vue 3 Composition API
- Naive UI（全域自動引入，無需手動 import 元件）
- Pinia（狀態管理）
- TypeScript + SCSS（`<script setup lang="ts">`、`<style scoped lang="scss">`）
- Nuxt 自動路由：`pages/` 目錄直接對應 URL

### 管理後台 (heartshop-admin)

- Vue 3 + Vite、Composition API
- Naive UI（`unplugin-vue-components` 自動引入）
- Pinia、TypeScript、SCSS
- 路由：`src/router/index.ts` 手動定義

### 後端 (HeartShop-Spring)

- Java 21、Spring Boot 3.5+、Maven
- MyBatis 3（Mapper Interface + XML，`resources/mapper/*.xml`）
- PostgreSQL 15/16
- JWT（`io.jsonwebtoken`）、Lombok、BCrypt 密碼加密
- Context path：`/api`（所有端點以 `/api/...` 開頭）

---

## 三、環境設定

環境變數統一放在 **根目錄 `.env`**，前後端子專案皆從此讀取，**不需要**在子目錄另建 `.env`。

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=heartshop
DB_USERNAME=postgres
DB_PASSWORD=...

JWT_SECRET=...

VITE_API_BASE_URL=http://localhost:8080   # 不含 /api
VITE_ADMIN_URL=http://localhost:5173
```

後端透過 `application.properties` 的 `${DB_HOST:localhost}` 形式讀取，**不使用** `application-local.yml`。

---

## 四、前端開發規範

### API 呼叫

前端商城統一使用 `services/http/client.ts` 的 `apiFetch`：

```typescript
// 自動讀取 cookie "token" 附加 Bearer header
// 自動解包 ApiResponse.data，直接回傳 T
import { apiFetch } from '@/services/http/client'

const data = await apiFetch<Product[]>('/api/products', { method: 'GET' })
```

- Nuxt dev proxy 將 `/api/*` 轉發至 `http://localhost:8080/api/*`
- `$fetch('/api/...')` 同樣走 proxy（`pages/shop/[slug].vue` 使用此方式）
- 圖片路徑：上傳檔案存於後端，前端存取 `/api/uploads/...` 路徑，proxy 轉發至後端

### 圖片 URL 處理

```typescript
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function toFullUrl(url?: string): string {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/uploads')) return `${apiBase}/api${url}`
  if (url.startsWith('/')) return `${apiBase}${url}`
  return `${apiBase}/api/${url}`
}
```

### 身份驗證（前端商城）

```typescript
const token      = useCookie<string | null>('token')
const memberInfo = useCookie<{ memberId: number; email: string; name: string; role?: string } | null>('memberInfo')

const isLoggedIn = computed(() => !!token.value)
const isAdmin    = computed(() => memberInfo.value?.role === 'ADMIN')
```

`apiFetch` 自動從 cookie 讀取 token 附加於 header，無需手動處理。

### 身份驗證（管理後台）

管理後台使用 `localStorage`：

```typescript
const token = localStorage.getItem('admin_token')
// 每次 fetch 手動附加
headers: { 'Authorization': `Bearer ${token}` }
```

### Naive UI 使用規範

- 前端商城：元件全域自動引入，直接在 template 使用 `<n-button>` 等，**無需 import**
- 管理後台：同樣透過 `unplugin-vue-components` 自動引入
- 若需 composable（`useMessage`、`useDialog`），仍需顯式 import：
  ```typescript
  import { useMessage } from 'naive-ui'
  const message = useMessage()
  ```

### 樣式規範

- 深色文字統一使用 `#353535`（不使用純黑 `#000`）
- 主色調：暖灰色系 `#8A897C`
- `*` 全域 cursor reset 已設定於 `assets/scss/layout/layout.scss`：
  非互動元素 `cursor: default`、連結/按鈕 `cursor: pointer`、輸入框 `cursor: text`
- 刪除/移除類按鈕樣式：紅色外框 `border: 1px solid #c0392b`，hover 時填滿

---

## 五、後端開發規範

### 分層架構

```
Controller → Service → Mapper（XML）
```

- Controller：只處理 HTTP 請求/回應，**不寫業務邏輯**
- Service：業務邏輯、交易管理
- Mapper：資料庫操作，SQL 全部寫在 `resources/mapper/*.xml`，**不在 Java 程式碼中硬編碼 SQL**

### 統一 API 回應格式

所有端點回傳 `ApiResponse<T>`：

```json
{
  "code": "0000",
  "message": "成功",
  "data": { ... },
  "timestamp": "2025-05-23T12:00:00"
}
```

- 成功：`code: "0000"`
- 前端 `apiFetch` 自動解包 `response.data`，直接得到 `T`

### 資料庫注意事項

- 表名 `orders` 和 `order_item` 在 SQL 中須用雙引號：`"orders"`、`"order_item"`（已在所有 Mapper XML 中套用）
- MyBatis 設定 `map-underscore-to-camel-case=true`，DB 欄位 `member_id` 對應 Java 屬性 `memberId`
- Mapper XML 中的多條件查詢使用 `<where>/<if>` 動態 SQL
- 多結果集關聯使用 `<resultMap>` + `<collection>`

### JWT 驗證

從 `Authorization: Bearer <token>` header 取得，`JwtUtil` 解析後得到 `memberId`（Long 型別）。需要驗證的端點在 Controller 方法參數加上 `@RequestHeader("Authorization") String authHeader`，再呼叫 `jwtUtil.getMemberId(token)`。

---

## 六、資料庫結構摘要

完整建表腳本：`heartshopsql/schema.sql`

| 資料表 | 說明 |
|--------|------|
| `member` | 會員，role: CUSTOMER / VIP / ADMIN |
| `category` | 商品分類，支援父子階層（`parent_id` self FK） |
| `product` | 商品主檔，含庫存、標籤、排序 |
| `product_image` | 商品圖片（一對多，ON DELETE CASCADE） |
| `cart_item` | 購物車 |
| `"orders"` | 訂單主檔（quoted） |
| `"order_item"` | 訂單明細，快照商品名稱與價格（quoted） |
| `wishlist` | 收藏，UNIQUE(member_id, product_id) |
| `home_block` | 首頁區塊（輪播 / 廣告 / 推薦商品） |
| `home_block_product` | 首頁推薦商品關聯 |
| `inventory_log` | 庫存異動記錄 |
| `product_qa` | 商品問答 |
| `order_qa` | 訂單問答（以 `order_no` VARCHAR 關聯，非 FK） |

---

## 七、已實作功能清單

### 前端商城

| 功能 | 路徑 |
|------|------|
| 首頁（輪播 + 商品推薦） | `/` |
| 商品分類列表 | `/shop/[slug]` |
| 商品搜尋 | `/search?q=...` |
| 商品詳情 + 問答 | `/product/[id]` |
| 購物車 | `/cart` |
| 訂單確認 | `/cart/confirm` |
| 訂單完成 | `/cart/checkout` |
| 會員中心 | `/member` |
| 會員訂單記錄 | `/member/orders` |
| 會員收藏清單 | `/member/wishlist` |
| 會員問答紀錄 | `/member/qa` |
| 品牌故事 | `/about` |
| 購物說明 | `/ShopInfo/shopinfo` |
| 門市收購表單 | `/GoodsUpload/goodsupload` |

### 管理後台

| 功能 | 路徑 |
|------|------|
| 儀表板 | `/` |
| 商品管理 | `/products` |
| 分類管理 | `/categories` |
| 庫存管理 | `/inventory` |
| 訂單管理 | `/orders` |
| 會員管理 | `/members` |
| 問答管理（商品 + 訂單） | `/qa` |
| 首頁區塊管理 | `/home-blocks` |

---

## 八、修改準則

1. **不破壞現有流程**：修改前確認上下游呼叫關係，確保現有 API 端點與前端呼叫路徑維持相容
2. **不引入新 UI 庫**：已有 Naive UI，新增元件優先使用現有元件庫
3. **不在子目錄建 .env**：環境變數統一由根目錄 `.env` 管理
4. **不在 Service 層寫 SQL**：所有 SQL 寫在 Mapper XML
5. **不在前端 hardcode API base URL**：一律讀取 `import.meta.env.VITE_API_BASE_URL`
6. **圖片路徑**：存入資料庫時存相對路徑（`/api/uploads/...`），前端顯示時加上 `VITE_API_BASE_URL`

---

## 九、溝通規範

- 語言：所有說明、commit message、與使用者對話一律使用**繁體中文**
- 架構層級的重大異動，先說明方案讓使用者確認後再執行
- 單純的修改或查閱，直接執行，不需等待確認
