// services/product.ts
import { apiFetch } from "./http/client";

export interface Product {
  productId: number;
  categoryId?: number;
  categoryName?: string;
  code: string;
  name: string;
  nameEn?: string;
  price?: number;
  discountPrice?: number;
  description?: string;
  sizeInfo?: string;
  tags?: string[];
  isNew?: boolean;
  isSoldOut?: boolean;
  isActive?: boolean;
  viewCount?: number;
  sortOrder?: number;
  imageUrl?: string;
  images?: string[]; // 商品圖片陣列
  createdAt?: string;
  updatedAt?: string;
  quantity?: number; // 前端購物車用數量
}

export const productService = {
  /**
   * 取得商品列表
   */
  getProducts(params?: {
    categoryId?: number;
    isActive?: boolean;
    isNew?: boolean;
    keyword?: string;
    page?: number;
    pageSize?: number;
    sortBy?: string;
    sortOrder?: string;
  }) {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value));
        }
      });
    }

    return apiFetch<any>(`/api/products?${queryParams.toString()}`, {
      method: "GET",
    });
  },

  /**
   * 根據 ID 取得商品詳細資訊
   */
  getProductById(id: number) {
    return apiFetch<Product>(`/api/products/${id}`, {
      method: "GET",
    });
  },
};
