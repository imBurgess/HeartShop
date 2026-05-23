import { apiFetch } from "./http/client";

export interface CartItem {
  cartItemId: number;
  productId: number;
  name: string;
  image: string;
  price: number;
  sizeName: string;
  quantity: number;
}

export interface AddToCartRequest {
  productId: number;
  sizeName: string;
  quantity: number;
}

export interface PlaceOrderRequest {
  receiverName: string;
  receiverPhone: string;
  receiverCity: string;
  receiverDistrict: string;
  receiverAddress: string;
  receiverNote: string;
  paymentMethod: string;
  shippingMethod: string;
}

// 修改此處：新增可選的 paymentHtml 屬性
export interface PlaceOrderResponse {
  orderNo: string;
  totalAmount: number;
  createdAt: string;
  paymentHtml?: string; // 接收後端手動產生的綠界 AIO CheckOut Form HTML 字串
}

export const cartService = {
  getCartItems() {
    return apiFetch<CartItem[]>("/api/cart", { method: "GET" });
  },

  addToCart(data: AddToCartRequest) {
    return apiFetch<void>("/api/cart", {
      method: "POST",
      body: data,
    });
  },

  updateQuantity(cartItemId: number, quantity: number) {
    return apiFetch<void>(`/api/cart/${cartItemId}`, {
      method: "PUT",
      body: { quantity },
    });
  },

  removeCartItem(cartItemId: number) {
    return apiFetch<void>(`/api/cart/${cartItemId}`, {
      method: "DELETE",
    });
  },

  clearCart() {
    return apiFetch<void>("/api/cart", {
      method: "DELETE",
    });
  },

  // 此處不需更動，TypeScript 會自動推導回傳型別包含 paymentHtml
  placeOrder(data: PlaceOrderRequest) {
    return apiFetch<PlaceOrderResponse>("/api/orders", {
      method: "POST",
      body: data,
    });
  },
};
