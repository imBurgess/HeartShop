import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { cartService, type CartItem } from "~/services/cart";

export const useCartStore = defineStore("cart", () => {
  const items = ref<CartItem[]>([]);
  const loading = ref(false);

  const fetchCart = async () => {
    try {
      loading.value = true;
      const data = await cartService.getCartItems();
      items.value = data || [];
    } catch (e) {
      console.error("載入購物車失敗", e);
    } finally {
      loading.value = false;
    }
  };

  const addToCart = async (productId: number, sizeName: string, quantity: number) => {
    try {
      loading.value = true;
      await cartService.addToCart({ productId, sizeName, quantity });
      await fetchCart();
      return true;
    } catch (e: any) {
      console.error("加入購物車失敗", e);
      return false;
    } finally {
      loading.value = false;
    }
  };

  const updateQuantity = async (cartItemId: number, quantity: number) => {
    try {
      loading.value = true;
      await cartService.updateQuantity(cartItemId, quantity);
      await fetchCart();
    } catch (e) {
      console.error("更新數量失敗", e);
    } finally {
      loading.value = false;
    }
  };

  const removeItem = async (cartItemId: number) => {
    try {
      loading.value = true;
      await cartService.removeCartItem(cartItemId);
      await fetchCart();
    } catch (e) {
      console.error("刪除商品失敗", e);
    } finally {
      loading.value = false;
    }
  };

  const clearCart = () => {
    items.value = [];
  };

  const totalQty = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0);
  });

  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
  });

  return {
    items,
    loading,
    fetchCart,
    addToCart,
    updateQuantity,
    removeItem,
    clearCart,
    totalQty,
    totalAmount,
  };
});
