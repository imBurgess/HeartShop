const API_BASE = "/api";

export interface AdminQaItem {
  qaId: number;
  productId?: number;
  productName?: string | null;
  orderNo?: string;
  memberId: number | null;
  memberName: string | null;
  question: string;
  answer: string | null;
  isPublic: boolean;
  createdAt: string;
  answeredAt: string | null;
}

export interface QaListResponse {
  items: AdminQaItem[];
  total: number;
  page: number;
  pageSize: number;
}

async function request<T>(url: string, opts: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem("admin_token");
  const res = await fetch(url, {
    ...opts,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...opts.headers,
    },
  });
  const json = await res.json();
  if (!res.ok || json.code !== "0000") {
    throw new Error(json.message || "請求失敗");
  }
  return json.data;
}

export const qaApi = {
  // 商品問答
  getProductQa(params: { page?: number; pageSize?: number; answered?: boolean | null }): Promise<QaListResponse> {
    const q = new URLSearchParams();
    if (params.page) q.set("page", String(params.page));
    if (params.pageSize) q.set("pageSize", String(params.pageSize));
    if (params.answered != null) q.set("answered", String(params.answered));
    return request<QaListResponse>(`${API_BASE}/admin/qa/products?${q}`);
  },

  answerProductQa(qaId: number, answer: string): Promise<void> {
    return request<void>(`${API_BASE}/admin/qa/products/${qaId}/answer`, {
      method: "PUT",
      body: JSON.stringify({ answer }),
    });
  },

  // 訂單問答
  getOrderQa(params: { page?: number; pageSize?: number; answered?: boolean | null }): Promise<QaListResponse> {
    const q = new URLSearchParams();
    if (params.page) q.set("page", String(params.page));
    if (params.pageSize) q.set("pageSize", String(params.pageSize));
    if (params.answered != null) q.set("answered", String(params.answered));
    return request<QaListResponse>(`${API_BASE}/admin/qa/orders?${q}`);
  },

  answerOrderQa(qaId: number, answer: string): Promise<void> {
    return request<void>(`${API_BASE}/admin/qa/orders/${qaId}/answer`, {
      method: "PUT",
      body: JSON.stringify({ answer }),
    });
  },

  deleteProductQa(qaId: number): Promise<void> {
    return request<void>(`${API_BASE}/admin/qa/products/${qaId}`, { method: "DELETE" });
  },

  deleteOrderQa(qaId: number): Promise<void> {
    return request<void>(`${API_BASE}/admin/qa/orders/${qaId}`, { method: "DELETE" });
  },
};
