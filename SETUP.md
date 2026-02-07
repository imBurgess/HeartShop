# HeartShop 專案初始化設定指南

## 📋 目錄

- [系統需求](#系統需求)
- [必要軟體安裝](#必要軟體安裝)
- [環境設定](#環境設定)
- [專案安裝與啟動](#專案安裝與啟動)
- [驗證安裝](#驗證安裝)
- [常見問題](#常見問題)

---

## 系統需求

- **作業系統**: Windows 10/11（或 macOS/Linux）
- **RAM**: 最少 8GB（建議 16GB）
- **硬碟空間**: 至少 10GB 可用空間

---

## 必要軟體安裝

### 1. Java Development Kit (JDK) 21

#### 下載與安裝

1. 前往 [Oracle JDK 21 下載頁面](https://www.oracle.com/java/technologies/downloads/#java21) 或使用 [OpenJDK](https://adoptium.net/)
2. 下載適合你作業系統的安裝檔（Windows x64 Installer）
3. 執行安裝程式，建議安裝路徑：`C:\Program Files\Java\jdk-21`

#### 設定環境變數

```powershell
# 在 PowerShell（系統管理員）執行：

# 設定 JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")

# 加入 PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = $currentPath + ";%JAVA_HOME%\bin"
[System.Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
```

#### 驗證安裝

```powershell
# 重開 PowerShell 後執行
java -version
# 應顯示：java version "21.x.x"
```

---

### 2. Apache Maven

#### 下載與安裝

1. 前往 [Maven 下載頁面](https://maven.apache.org/download.cgi)
2. 下載 Binary zip archive（例如：apache-maven-3.9.6-bin.zip）
3. 解壓縮到 `C:\Program Files\Apache\Maven`

#### 設定環境變數

```powershell
# 在 PowerShell（系統管理員）執行：

# 設定 MAVEN_HOME
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\Maven\apache-maven-3.9.6", "Machine")

# 加入 PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = $currentPath + ";%MAVEN_HOME%\bin"
[System.Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
```

#### 驗證安裝

```powershell
# 重開 PowerShell 後執行
mvn -version
# 應顯示：Apache Maven 3.9.x
```

---

### 3. Node.js（含 npm）

#### 下載與安裝

1. 前往 [Node.js 官網](https://nodejs.org/)
2. 下載 **LTS 版本**（目前建議 20.x 或以上）
3. 執行安裝程式（會自動安裝 npm）

#### 驗證安裝

```powershell
node -v
# 應顯示：v20.x.x 或更新

npm -v
# 應顯示：10.x.x 或更新
```

---

### 4. PostgreSQL 資料庫

#### 下載與安裝

1. 前往 [PostgreSQL 下載頁面](https://www.postgresql.org/download/windows/)
2. 下載安裝程式（建議使用 PostgreSQL 15 或 16）
3. 執行安裝程式：
   - 記住你設定的 **密碼**（預設使用者為 `postgres`）
   - 預設埠號：`5432`
   - 安裝 pgAdmin 4（圖形化管理工具）

#### 建立專案資料庫

```sql
-- 使用 pgAdmin 或 psql 執行以下 SQL：

CREATE DATABASE heartshop;

-- 建立專用使用者（選擇性，但建議）
CREATE USER heartshop_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE heartshop TO heartshop_user;
```

#### 驗證安裝

```powershell
# 在命令列執行
psql -U postgres -c "SELECT version();"
# 應顯示 PostgreSQL 版本資訊
```

---

### 5. Git（版本控制）

#### 下載與安裝

1. 前往 [Git 官網](https://git-scm.com/download/win)
2. 下載並安裝 Git for Windows
3. 安裝過程中保持預設設定即可

#### 驗證安裝

```powershell
git --version
# 應顯示：git version 2.x.x
```

---

### 6. IDE 安裝（建議）

#### 後端開發 - IntelliJ IDEA

1. 前往 [IntelliJ IDEA 下載](https://www.jetbrains.com/idea/download/)
2. 下載 Community Edition（免費）或 Ultimate Edition
3. 安裝並設定 JDK 21

#### 前端開發 - Visual Studio Code

1. 前往 [VS Code 官網](https://code.visualstudio.com/)
2. 下載並安裝
3. 建議安裝以下擴充套件：
   - Vue - Official
   - ESLint
   - Prettier

---

## 環境設定

### 1. 克隆專案（若尚未複製）

```powershell
git clone <你的專案儲存庫 URL>
cd heart-shop
```

### 2. 後端設定（Spring Boot）

#### 建立資料庫連線設定檔

在 `HeartShop-Spring/HeartShop/src/main/resources/` 目錄下建立 `application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/heartshop
    username: postgres # 或你建立的 heartshop_user
    password: your_password # 替換成你的密碼
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# 檔案上傳設定
upload:
  path: D:/heartshop/uploads # 根據你的需求調整路徑
```

> ⚠️ **重要**：請將 `application-local.yml` 加入 `.gitignore`，避免敏感資訊被提交到版本控制。

#### 修改主設定檔

確保 `application.yml` 或 `application.properties` 有以下設定：

```yaml
spring:
  profiles:
    active: local # 啟用 local profile
```

---

## 專案安裝與啟動

### 1. 後端（Spring Boot）

```powershell
# 進入後端專案目錄
cd HeartShop-Spring/HeartShop

# 安裝相依套件（首次執行或 pom.xml 有更新時）
mvn clean install

# 啟動後端服務
mvn spring-boot:run

# 或者編譯成 jar 後執行
mvn package
java -jar target/HeartShop-0.0.1-SNAPSHOT.jar
```

**預期輸出**：

- 服務應該在 `http://localhost:8080` 啟動
- 看到 "Started HeartShopApplication in X.XXX seconds"

---

### 2. 前端（heartshop-frontend）

```powershell
# 開啟新的終端機，進入前端目錄
cd heartshop-frontend

# 安裝相依套件
npm install

# 啟動開發伺服器
npm run dev
```

**預期輸出**：

- 前端應該在 `http://localhost:3000` 啟動
- Nuxt 開發伺服器成功運行

---

### 3. 管理後台（heartshop-admin）

```powershell
# 開啟新的終端機，進入管理後台目錄
cd heartshop-admin

# 安裝相依套件
npm install

# 啟動開發伺服器
npm run dev
```

**預期輸出**：

- 管理後台應該在 `http://localhost:5173` 啟動（Vite 預設埠號）

---

## 驗證安裝

### 檢查清單

- [ ] **Java**: `java -version` 顯示 21.x.x
- [ ] **Maven**: `mvn -version` 顯示 3.9.x
- [ ] **Node.js**: `node -v` 顯示 v20.x 或更新
- [ ] **npm**: `npm -v` 顯示 10.x 或更新
- [ ] **PostgreSQL**: 資料庫服務正在運行，可以連線
- [ ] **後端**: 訪問 `http://localhost:8080/api/health`（如果有健康檢查端點）
- [ ] **前端**: 訪問 `http://localhost:3000` 可看到頁面
- [ ] **管理後台**: 訪問 `http://localhost:5173` 可看到登入頁面

---

## 常見問題

### Q1: Maven 下載相依套件很慢

**A**: 更改為國內鏡像源（如阿里雲）

編輯 `C:\Users\你的使用者名稱\.m2\settings.xml`（若不存在則建立）：

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

### Q2: npm install 安裝失敗

**A**: 清除快取後重試

```powershell
npm cache clean --force
npm install
```

或改用 yarn：

```powershell
npm install -g yarn
yarn install
```

### Q3: 後端啟動時資料庫連線失敗

**A**: 檢查以下項目

1. PostgreSQL 服務是否啟動：
   ```powershell
   # Windows 服務管理
   Get-Service postgresql*
   ```
2. 資料庫名稱、使用者名稱、密碼是否正確
3. 埠號是否為 5432（或你設定的埠號）

### Q4: Port 已被佔用

**A**: 找出並關閉佔用的程式

```powershell
# 查看佔用 8080 的程式
netstat -ano | findstr :8080

# 關閉程式（PID 替換成上面查到的）
taskkill /PID <PID> /F
```

### Q5: Windows 防火牆阻擋連線

**A**: 新增例外規則

1. 開啟「Windows Defender 防火牆」
2. 點選「進階設定」
3. 新增「輸入規則」，允許 8080、3000、5173 埠號

---

## 🎉 完成！

現在你應該已經成功設定好開發環境了。若有任何問題，請參考專案的 README.md 或聯繫專案維護者。

**開發流程建議**：

1. 先啟動 PostgreSQL
2. 再啟動後端服務（Spring Boot）
3. 最後啟動前端（frontend 或 admin）

**快速啟動腳本**（可選）：
你可以建立一個 `start-all.ps1` 腳本來一鍵啟動所有服務：

```powershell
# start-all.ps1
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd HeartShop-Spring/HeartShop; mvn spring-boot:run"
Start-Sleep -Seconds 10
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd heartshop-frontend; npm run dev"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd heartshop-admin; npm run dev"
```

使用方式：

```powershell
.\start-all.ps1
```
