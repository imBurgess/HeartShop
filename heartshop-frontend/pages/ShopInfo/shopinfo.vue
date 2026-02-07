<template>
  <main class="shopping-guide">
    <section class="guide-container">
      <div class="guide-header">
        <span class="title-bar" />
        <h1>購物說明</h1>
      </div>

      <!-- 折疊列表 -->
      <n-collapse accordion class="guide-collapse">
        <!-- 付款方式 -->
        <n-collapse-item name="payment">
          <template #header>
            <span class="collapse-title">付款方式</span>
          </template>
          <div class="guide-content">
            <ul class="guide-list">
              <li>
                信用卡即時線上一次刷卡付款方便又好用的付款方式，當您選擇線上刷卡方式進行交易時，作業流程透過
                SSL 加密機制，保障您的個人隱私資料。
              </li>
              <li>
                超商付款取貨適合不方便在家收貨或者不方便使用 ATM
                與無信用卡的消費者。
              </li>
              <li>
                LINE Pay 付款不需輸入信用卡資訊及 3D
                驗證，僅需輸入專屬密碼即可使用信用卡進行付款，節省您的付款時間。
              </li>
              <li>
                街口支付請先備妥您行動裝置中的街口支付應用程式，可使用街口儲值帳戶
                / 銀行帳戶，並搭配街口幣進行付款。
              </li>
              <li>
                Apple Pay 請先備妥您行動裝置中的 Apple Pay
                Wallet，可使用綁定的信用卡結帳，交易過程全程加密，交易安全有保障。
              </li>
              <li>
                AFTEE
                先享後付利用手機號碼認證就能下單，還能先收到商品後再利用電子繳費單付款。加入
                AFTEE 先享後付 App 會員額外享繳費月結制、無卡分期等專屬優惠。
              </li>
              <li>
                悠遊付收款方便快速又安全便利，由 20 年悠遊卡公司提供服務。
              </li>
            </ul>
          </div>
        </n-collapse-item>

        <!-- 運送方式 -->
        <n-collapse-item name="shipping">
          <template #header>
            <span class="collapse-title">運送方式</span>
          </template>
          <div class="guide-content">
            <ul class="guide-list">
              <li>
                帳款確認後將立即處理您的訂單，您將於七個工作日內（不含六日及國定假日）收到訂購之商品。
              </li>
              <li>
                商品寄送方式有「宅配」、「超商取貨」2
                種，恕無法指定商品到達日期及時間。※選擇「宅配」，周末及國定假日無提供配送服務。
              </li>
              <li>
                商品將由門市直接出貨，如您的購物車內含有 2
                個以上不同門市販售的商品，將會分開為您寄出，還請留意包裹抵達時間會不同。
              </li>
              <li>我們提供超商取貨、宅配服務。出貨時間為週一至週五。</li>
            </ul>
          </div>
        </n-collapse-item>

        <!-- 退換貨方式 -->
        <n-collapse-item name="return">
          <template #header>
            <span class="collapse-title">退換貨方式</span>
          </template>
          <div class="guide-content">
            <ul class="guide-list">
              <li>根據台灣消費者保護法之規定 :
                提供您商品到貨七天鑑賞期的權益，並提醒您注意，商品退換貨時必須恢復原狀，也就是您收到商品時的全新狀態，包括 : 吊牌未剪，商品、內外包裝、文件、贈品皆須為全新未使用的狀態，否則無法退換貨，且退換貨以一次為限。</li>
            </ul>
          </div>
        </n-collapse-item>

        <!-- 下單前務必閱讀（橘色小方塊） -->
        <n-collapse-item name="must-read">
          <template #header>
            <span class="collapse-title">下單前務必閱讀</span>
          </template>
          <div class="guide-content">
            <ul class="guide-list">
              <li>
                本商店為二手店，商品均只有 1
                件，下單後將由專人確認訂單，本公司確認後且訂單狀態顯示已出貨或配送中訂單始成立。
              </li>
              <li>
                商品與實體店鋪同步販售，不能保證下單後商品一定有庫存！如遇下單後無庫存，會透過
                Email
                或簡訊通知您取消訂單並處理退款事宜，如期待有貨，請斟酌後再下單。
              </li>
              <li>
                因為二手商品，商品狀態較易與您所想有所出入，若客服人員透過 Email
                或簡訊通知您確認商品狀態，須於 Email
                指定期限內完成確認，若未回覆購買意願，將有可能取消訂單。
              </li>
              <li>
                如對商品瑕疵、狀態有所疑慮，請多加詢問客服人員後再下單，避免後續退貨等不便。
              </li>
              <li>
                商品將由門市直接出貨，如您的購物車內含有 2
                個以上不同門市販售的商品，將會分開為您寄出，包裹抵達時間會不同，恕無法指定商品同時送達及抵達時間。如有疑慮，請斟酌後再下單。
              </li>
              <li>
                因商品重量為預設，非實際重量，如想了解商品重量，歡迎透過會員中心與客服聯繫以便協助您。
              </li>
              <li>
                因系統有防護機制，惡意退貨或是逾期不取可能被系統限制下單，還請注意。
              </li>
            </ul>
          </div>
        </n-collapse-item>

        <!-- 訂單進度 -->
        <n-collapse-item name="status">
          <template #header>
            <span class="collapse-title">訂單進度</span>
          </template>
          <div class="guide-content">
            <ul class="guide-list">
              <li>
                <strong>是否訂購成功：</strong>
                訂單成立後，系統會發送「訂單通知」電子郵件給您，記載您提交的訂單詳細資料，包括訂購明細和收件資訊。
              </li>
              <li>
                <strong>如何追蹤訂單進度：</strong>
                有關商品的出貨進度，可在「我的帳戶 &gt; 訂單查詢」中查詢。
              </li>
              <li>
                <strong>如何取消訂單：</strong>
                商品尚未出貨前，可於「我的帳戶 &gt;
                訂單查詢」中，點選「取消訂單」按鈕。
              </li>
            </ul>
          </div>
        </n-collapse-item>

        <!-- 電子發票專戶 -->
        <n-collapse-item name="e-invoice">
          <template #header>
            <span class="collapse-title">電子發票專戶</span>
          </template>
          <div class="guide-content">
            <p class="section-title">小提醒：</p>
            <ul class="guide-list">
              <li>
                按下會員載具歸戶，系統將自動連結至政府電子發票平台進行歸戶。
              </li>
              <li>
                歸戶完成後，您即可在政府電子發票平台，查詢所有本店會員卡的電子發票。
              </li>
              <li>已進行歸戶的電子發票，仍可在本店查詢所有的消費明細。</li>
              <li>歸戶後的中獎發票將由財稅中心通知，本公司不再另行通知。</li>
            </ul>

            <p class="section-title">歸戶查詢：</p>
            <ul class="guide-list">
              <li>
                財政部電子發票整合平台（https://www.einvoice.nat.gov.tw/）
              </li>
              <li>財稅中心 24 小時免費服務電話 0800-52-1988</li>
            </ul>
          </div>
        </n-collapse-item>
      </n-collapse>
    </section>
  </main>
</template>

<style scoped lang="scss">
.shopping-guide {
  padding: 48px 0 80px;

  .guide-container {
    width: min(1000px, 90%);
    margin: 0 auto;

    .guide-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 24px;

      h1 {
        font-size: xx-large;
        letter-spacing: 0.08em;
      }
    }

    .title-bar {
      width: 4px;
      height: 30px;
      background: #333;
    }

    .guide-collapse {
      :deep(.n-collapse-item__header) {
        background-color: #633f29b3;
        --n-arrow-color: #ffffff;
        cursor: pointer;
        user-select: none;
        align-items: center;
        min-height: 60px;
        padding: 0 24px;
        border: none;
      }

      .collapse-title {
        font-size: large;
        letter-spacing: 0.08em;
        color: #ffffff;
      }

      /* 內容區 */
      :deep(.n-collapse-item__content-inner) {
        padding: 10px 24px 16px 24px;
        background: #633f29b3;
        border-top: 1px solid rgba(255, 255, 255, 0.12);
      }

      /* 內文文字 */
      .guide-content {
        font-size: medium;
        line-height: 1.7;
        color: #ffffff;

        .guide-list {
          margin: 0;
          padding-left: 1.4rem; // 控制縮排
        }

        .guide-list li {
          margin-bottom: 0.5em;
        }

        .section-title {
          font-weight: 600;
          margin-top: 0.75em;
          margin-bottom: 0.25em;
        }
      }
    }
  }
}
</style>
<script setup lang="ts">
</script>