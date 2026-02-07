// 庫存管理 API 服務
import type { Product } from "./product";

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

    const response = await fetch(
      `http://localhost:8080/api/inventory?${queryString}`,
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
    const response = await fetch("http://localhost:8080/api/inventory/alerts", {
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
    const response = await fetch(
      `http://localhost:8080/api/inventory/${productId}/adjust`,
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
    const response = await fetch(
      `http://localhost:8080/api/inventory/${productId}/logs?page=${page}&pageSize=${pageSize}`,
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
