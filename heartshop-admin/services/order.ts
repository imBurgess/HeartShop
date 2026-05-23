const API_BASE = "/api";

export interface AdminOrder {
  orderId: number;
  orderNo: string;
  memberId: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  totalAmount: number;
  status: string;
  paymentMethod: string;
  shippingMethod: string;
  createdAt: string;
  updatedAt: string;
  items?: AdminOrderItem[];
}

export interface AdminOrderItem {
  orderItemId: number;
  productId: number;
  productName: string;
  productImage: string;
  sizeName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface OrderListResponse {
  items: AdminOrder[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface OrderQueryParams {
  keyword?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  pageSize?: number;
}

async function apiFetch<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const json = await res.json();
  return json.data as T;
}

export const orderApi = {
  async getOrders(params: OrderQueryParams = {}): Promise<OrderListResponse> {
    const q = new URLSearchParams();
    if (params.keyword) q.set("keyword", params.keyword);
    if (params.status) q.set("status", params.status);
    if (params.startDate) q.set("startDate", params.startDate);
    if (params.endDate) q.set("endDate", params.endDate);
    q.set("page", String(params.page ?? 1));
    q.set("pageSize", String(params.pageSize ?? 20));
    return apiFetch<OrderListResponse>(`${API_BASE}/admin/orders?${q}`);
  },

  async getOrderDetail(orderNo: string): Promise<AdminOrder> {
    return apiFetch<AdminOrder>(`${API_BASE}/admin/orders/${orderNo}`);
  },

  async updateStatus(orderNo: string, status: string): Promise<void> {
    await apiFetch(`${API_BASE}/admin/orders/${orderNo}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    });
  },

  async deleteOrder(orderNo: string): Promise<void> {
    await apiFetch(`${API_BASE}/admin/orders/${orderNo}`, {
      method: "DELETE",
    });
  },
};
