import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
  },
  {
    path: "/",
    component: () => import("@/layouts/index.vue"),
    children: [
      {
        path: "",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
      },
      {
        path: "products",
        name: "ProductList",
        component: () => import("@/views/products/index.vue"),
      },
      {
        path: "products/create",
        name: "ProductCreate",
        component: () => import("@/views/products/form.vue"),
      },
      {
        path: "products/:id/edit",
        name: "ProductEdit",
        component: () => import("@/views/products/form.vue"),
      },
      {
        path: "orders",
        name: "OrderList",
        component: () => import("@/views/orders/index.vue"),
      },
      {
        path: "members",
        name: "MemberList",
        component: () => import("@/views/members/index.vue"),
      },
      {
        path: "categories",
        name: "CategoryList",
        component: () => import("@/views/categories/index.vue"),
      },
      {
        path: "inventory",
        name: "InventoryList",
        component: () => import("@/views/inventory/index.vue"),
      },
      {
        path: "qa",
        name: "QaManagement",
        component: () => import("@/views/qa/index.vue"),
      },
      {
        path: "home-blocks",
        name: "HomeBlockList",
        component: () => import("@/views/home-blocks/index.vue"),
      },
      {
        path: "settings",
        name: "Settings",
        component: () => import("@/views/settings/index.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;
