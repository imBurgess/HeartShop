# HeartShop 開發環境設定指南

## 專案架構

```
heart-shop/
├── .env                    # 統一環境變數（所有子專案共用）
├── .env.example            # 環境變數範本
├── heartshopsql/           # 資料庫 SQL 腳本
│   └── schema.sql          # 完整建表腳本（一次執行即可）
├── HeartShop-Spring/       # 後端 Spring Boot
├── heartshop-frontend/     # 前端 Nuxt 3
└── heartshop-admin/        # 管理後台 Vue 3 + Vite
```

**服務埠號**

| 服務 | 網址 |
|------|------|
| 後端 API | http://localhost:8080 |
| 前端 | http://localhost:3000 |
| 管理後台 | http://localhost:5173 |

---

## 系統需求

| 工具 | 版本 |
|------|------|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 20 LTS+ |
| PostgreSQL | 15 或 16 |

---

## 一、安裝必要軟體

### JDK 21

下載 [Eclipse Temurin 21](https://adoptium.net/) 或 [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)，安裝後設定環境變數：

```powershell
# PowerShell（系統管理員）
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21", "Machine")
$p = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", "$p;%JAVA_HOME%\bin", "Machine")
```

驗證：
```powershell
java -version   # java version "21.x.x"
```

---

### Maven

下載 [Maven Binary zip](https://maven.apache.org/download.cgi)，解壓後設定：

```powershell
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\tools\maven", "Machine")
$p = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", "$p;%MAVEN_HOME%\bin", "Machine")
```

驗證：
```powershell
mvn -version   # Apache Maven 3.9.x
```

---

### Node.js

下載 [Node.js LTS](https://nodejs.org/)（20.x 以上），安裝時勾選「Add to PATH」。

驗證：
```powershell
node -v   # v20.x.x
npm -v    # 10.x.x
```

---

### PostgreSQL

下載 [PostgreSQL 16](https://www.postgresql.org/download/windows/) 並安裝。安裝時：
- 記下設定的密碼（預設使用者為 `postgres`）
- 埠號維持預設 `5432`

建立資料庫：
```sql
-- 使用 pgAdmin 或 psql 執行
CREATE DATABASE heartshop;
```

---

## 二、設定環境變數

在專案根目錄 `heart-shop/` 複製範本並填入你的設定：

```powershell
copy .env.example .env
```

編輯 `.env`：

```dotenv
# 資料庫連線
DB_HOST=localhost
DB_PORT=5432
DB_NAME=heartshop
DB_USERNAME=postgres
DB_PASSWORD=你的密碼

# JWT 密鑰（正式環境請換成高強度隨機字串）
JWT_SECRET=HeartShop-Super-Secret-Key-For-Development-Environment-Change-This-In-Production-64Bytes

# API 伺服器地址（前端使用，不含 /api）
VITE_API_BASE_URL=http://localhost:8080

# 管理後台地址
VITE_ADMIN_URL=http://localhost:5173
```

> **注意**：`.env` 已加入 `.gitignore`，不會被提交。根目錄這一份 `.env` 由前後端所有子專案共用，**不需要**在子目錄另外建立。

---

## 三、建立資料庫資料表

確認 PostgreSQL 啟動且 `heartshop` 資料庫已建立後，執行建表腳本：

```powershell
# 方法一：psql 命令列
psql -U postgres -d heartshop -f heartshopsql/schema.sql

# 方法二：pgAdmin
# 開啟 pgAdmin → 選擇 heartshop 資料庫 → Query Tool → 貼入 schema.sql 內容執行
```

建立的資料表（依建立順序）：

| 資料表 | 說明 |
|--------|------|
| `member` | 會員帳號 |
| `category` | 商品分類（支援父子階層） |
| `product` | 商品主檔 |
| `product_image` | 商品圖片 |
| `cart_item` | 購物車 |
| `orders` | 訂單主檔 |
| `order_item` | 訂單明細 |
| `wishlist` | 收藏清單 |
| `home_block` | 首頁區塊（輪播 / 廣告）|
| `home_block_product` | 首頁推薦商品關聯 |
| `inventory_log` | 庫存異動記錄 |
| `product_qa` | 商品問答 |
| `order_qa` | 訂單問答 |

---

## 四、啟動服務

各服務需分別開啟終端機視窗執行，**啟動順序：後端 → 前端 → 管理後台**。

### 後端（Spring Boot）

```powershell
cd HeartShop-Spring/HeartShop
mvn spring-boot:run
```

看到 `Started HeartShopApplication in X.XXX seconds` 即代表啟動成功。
API 根路徑：`http://localhost:8080/api`

---

### 前端（Nuxt 3）

```powershell
cd heartshop-frontend
npm install   # 首次或 package.json 更新後執行
npm run dev
```

前端：`http://localhost:3000`

---

### 管理後台（Vue 3 + Vite）

```powershell
cd heartshop-admin
npm install   # 首次或 package.json 更新後執行
npm run dev
```

管理後台：`http://localhost:5173`

預設管理員帳號需透過資料庫直接建立，`member.role` 設為 `ADMIN`。

---

### 一鍵啟動腳本（選用）

在專案根目錄建立 `start-dev.ps1`：

```powershell
# start-dev.ps1
Start-Process powershell -ArgumentList "-NoExit -Command `"cd '$PWD\HeartShop-Spring\HeartShop'; mvn spring-boot:run`""
Start-Sleep -Seconds 15
Start-Process powershell -ArgumentList "-NoExit -Command `"cd '$PWD\heartshop-frontend'; npm run dev`""
Start-Process powershell -ArgumentList "-NoExit -Command `"cd '$PWD\heartshop-admin'; npm run dev`""
```

```powershell
.\start-dev.ps1
```

---

## 常見問題

### 後端啟動時資料庫連線失敗

1. 確認 PostgreSQL 服務正在執行：
   ```powershell
   Get-Service postgresql*
   ```
2. 確認 `.env` 中的 `DB_PASSWORD` 與 PostgreSQL 設定的密碼一致
3. 確認 `heartshop` 資料庫已建立

---

### Port 已被佔用

```powershell
# 查看佔用 8080 的程式
netstat -ano | findstr :8080

# 強制終止（PID 替換為上方查到的數字）
taskkill /PID <PID> /F
```

---

### npm install 失敗

```powershell
npm cache clean --force
npm install
```

---

### Maven 下載依賴很慢

在 `C:\Users\<使用者名稱>\.m2\settings.xml` 加入阿里雲鏡像：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

---

### 前端圖片無法顯示

確認後端已啟動，且 `.env` 中 `VITE_API_BASE_URL` 設定正確。
上傳的圖片儲存在後端的 `uploads/` 目錄，透過 `/api/uploads/...` 路徑存取。
