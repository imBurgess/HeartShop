<template>
  <NConfigProvider>
    <NMessageProvider>
      <NuxtLayout>
        <NuxtPage />
      </NuxtLayout>
    </NMessageProvider>
  </NConfigProvider>
</template>

<script setup lang="ts">
import { NConfigProvider, NMessageProvider } from "naive-ui";
</script>

<style>
html,
body,
#__nuxt {
  height: 100%;
}

body {
  background-color: #ffffffc8;
  -webkit-transition: background-color 2s ease-in;
  transition: background-color 2s ease-in;
}
</style>
