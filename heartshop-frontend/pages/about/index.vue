<template>
  <div class="about-page">
    <main class="about-card">
      <h3 class="about-title">關於拾心市集</h3>
      <p class="about-text">
        拾心市集是一個致力於推廣用心選物的網路平台，提供多元商品及安心購物體驗。歡迎聯絡我們，與我們一同打造美好的市集生活！
      </p>
      <ul class="about-list">
        <li>品牌故事 : 我想順利畢業</li>
        <li>團隊簡介 : 由 "你有心嗎?" 團隊嘔心瀝血製作</li>
        <li>聯絡方式 : 04-xxxxxx</li>
      </ul>
    </main>
  </div>
  <footer></footer>
</template>

<script setup></script>

<style lang="scss" scoped>
.about-page {
  display: flex;
  justify-content: center;
}

.about-card {
  margin: 50px auto;
  width: 350px;
  background: #633f29b3;
  padding: 30px 25px;
  border-radius: 20px;
}

.about-title,
.about-text,
.about-list {
  color: #ffffff;
}

.about-list {
  margin-top: 12px;
  padding-left: 20px;
  list-style: disc;
}
</style>
