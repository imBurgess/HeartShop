const API_BASE = "/api";

export interface AdminMember {
  memberId: number;
  email: string;
  name: string;
  phone: string;
  role: string;        // ADMIN | VIP | CUSTOMER
  status: string;      // ACTIVE | INACTIVE
  bonusPoints: number;
  totalOrders: number;
  totalSpent: number;
  createdAt: string;
  updatedAt: string;
}

export interface MemberListResponse {
  items: AdminMember[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
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

export const memberApi = {
  async getMembers(params: {
    keyword?: string;
    status?: string;
    page?: number;
    pageSize?: number;
  } = {}): Promise<MemberListResponse> {
    const q = new URLSearchParams();
    if (params.keyword) q.set("keyword", params.keyword);
    if (params.status) q.set("status", params.status);
    q.set("page", String(params.page ?? 1));
    q.set("pageSize", String(params.pageSize ?? 20));
    return apiFetch<MemberListResponse>(`${API_BASE}/admin/members?${q}`);
  },

  async getMember(memberId: number): Promise<AdminMember> {
    return apiFetch<AdminMember>(`${API_BASE}/admin/members/${memberId}`);
  },

  async updateStatus(memberId: number, status: string): Promise<void> {
    await apiFetch(`${API_BASE}/admin/members/${memberId}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    });
  },

  async updateRole(memberId: number, role: string): Promise<void> {
    await apiFetch(`${API_BASE}/admin/members/${memberId}/role`, {
      method: "PUT",
      body: JSON.stringify({ role }),
    });
  },

  async deleteMember(memberId: number): Promise<void> {
    await apiFetch(`${API_BASE}/admin/members/${memberId}`, {
      method: "DELETE",
    });
  },

  async createMember(data: {
    email: string;
    name: string;
    password: string;
    role: string;
  }): Promise<void> {
    await apiFetch(`${API_BASE}/admin/members`, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};
