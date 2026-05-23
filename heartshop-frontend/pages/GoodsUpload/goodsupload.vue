<template>
  <div class="form-page">
    <main class="form-card">
      <n-card title="商品上傳" embedded>
        <n-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-placement="top"
        >
          <!-- 姓名 -->
          <n-form-item label="姓名" path="username">
            <n-input v-model:value="form.username" placeholder="請輸入姓名" />
          </n-form-item>

          <!-- email -->
          <n-form-item label="Email" path="email">
            <n-input
              type="email"
              v-model:value="form.email"
              placeholder="請輸入 Email"
            />
          </n-form-item>

          <!-- 電話 -->
          <n-form-item label="聯絡電話" path="phone">
            <n-input v-model:value="form.phone" placeholder="可選填" />
          </n-form-item>

          <!-- 商品圖片上傳 -->
          <n-form-item label="商品圖片" path="imageList">
            <n-upload
              v-model:file-list="form.imageList"
              list-type="image-card"
              accept="image/*"
              :max="3"
              :default-upload="false"
            >
              上傳圖片
            </n-upload>
          </n-form-item>

          <!-- 留言 -->
          <n-form-item label="留言內容" path="message">
            <n-input
              type="textarea"
              v-model:value="form.message"
              rows="4"
              placeholder="輸入留言"
            />
          </n-form-item>

          <!-- 按鈕 -->
          <n-button block @click="handleSubmit" color="#353535">
            送出
          </n-button>
        </n-form>
      </n-card>
    </main>
  </div>
  <footer></footer>
</template>

<script setup>
import { reactive, ref } from "vue";

const formRef = ref(null);

const form = reactive({
  username: "",
  email: "",
  phone: "",
  message: "",
  imageList: [],
});

const rules = {
  username: {
    required: true,
    message: "請輸入姓名",
    trigger: "blur",
  },
  email: {
    required: true,
    message: "請輸入 Email",
    trigger: "blur",
  },
  imageList: {
    type: "array",
    required: true,
    min: 1,
    message: "請至少上傳一張商品圖片",
    trigger: "change",
  },
};

const handleSubmit = () => {
  formRef.value?.validate((errors) => {
    if (!errors) {
      window.alert("資料已送出！");
    }
  });
};
</script>
<style scoped lang="scss">
/* 表單卡片 */
.form-card {
  margin: 50px auto;
  width: min(400px, 90%);
}
::v-deep(.n-card) {
  background-color: #8a897c;
  border-radius: 16px;
  padding: 20px;
  border: none;
}

::v-deep(.n-form-item-label) {
  color: #353535;
  font-weight: bold;
  font-size: larger;
  --n--color: #ffffff;
}
::v-deep(.n-card-header__main) {
  color: #353535 !important;
  font-weight: bold;
  font-size: larger;
}
::v-deep(.n-upload .n-upload-trigger) {
  font-weight: bold;
  font-family: "Noto Sans TC", sans-serif;
  color: #353535;
}
</style>
