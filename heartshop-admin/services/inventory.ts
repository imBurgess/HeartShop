// 庫存管理 API 服務

/** 商品基本資料（與後端 ProductDTO 對應） */
export interface Product {
  productId: number;
  code?: string;
  name: string;
  nameEn?: string;
  description?: string;
  sizeInfo?: string;
  price?: number;
  discountPrice?: number;
  stock?: number;
  isActive?: boolean;
  isNew?: boolean;
  categoryId?: number;
  imageUrl?: string;
  images?: string[];
  tags?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryQueryParams {
  categoryId?: number;
  keyword?: string;
  lowStockOnly?: boolean;
  sortBy?: string;
  sortOrder?: string;
  page?: number;
  pageSize?: number;
}

export interface InventoryAdjustRequest {
  quantityChange: number;
  operator?: string;
  remark?: string;
}

export interface InventoryLog {
  logId: number;
  productId: number;
  productCode: string;
  productName: string;
  changeType: string;
  changeTypeDesc: string;
  quantityBefore: number;
  quantityChange: number;
  quantityAfter: number;
  operator?: string;
  remark?: string;
  createdAt: string;
}

export interface InventoryResponse {
  items: Product[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface InventoryLogResponse {
  items: InventoryLog[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export const inventoryApi = {
  // 查詢庫存列表
  async getInventoryList(params: InventoryQueryParams) {
    const queryString = new URLSearchParams(
      Object.entries(params).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null) {
          acc[key] = String(value);
        }
        return acc;
      }, {} as Record<string, string>)
    ).toString();

    const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const response = await fetch(
      `${apiBase}/api/inventory?${queryString}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch inventory list");
    }

    const result = await response.json();
    return result.data as InventoryResponse;
  },

  // 取得低庫存警示
  async getLowStockAlerts() {
    const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const response = await fetch(`${apiBase}/api/inventory/alerts`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch low stock alerts");
    }

    const result = await response.json();
    return result.data as Product[];
  },

  // 調整庫存
  async adjustStock(productId: number, request: InventoryAdjustRequest) {
    const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const response = await fetch(
      `${apiBase}/api/inventory/${productId}/adjust`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(request),
      }
    );

    if (!response.ok) {
      throw new Error("Failed to adjust stock");
    }

    const result = await response.json();
    return result.data as Product;
  },

  // 查詢庫存異動記錄
  async getInventoryLogs(
    productId: number,
    page: number = 1,
    pageSize: number = 20
  ) {
    const apiBase = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const response = await fetch(
      `${apiBase}/api/inventory/${productId}/logs?page=${page}&pageSize=${pageSize}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch inventory logs");
    }

    const result = await response.json();
    return result.data as InventoryLogResponse;
  },
};
