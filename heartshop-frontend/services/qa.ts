import { apiFetch } from './http/client'

export interface QaItem {
  qaId: number
  productId?: number
  productName?: string | null
  orderNo?: string
  memberId: number | null
  memberName: string | null
  question: string
  answer: string | null
  isPublic: boolean
  createdAt: string
  answeredAt: string | null
}

export const qaService = {
  async getQa(productId: number): Promise<QaItem[]> {
    return apiFetch<QaItem[]>(`/api/products/${productId}/qa`, { method: 'GET' })
  },

  async addQuestion(productId: number, question: string): Promise<QaItem> {
    return apiFetch<QaItem>(`/api/products/${productId}/qa`, {
      method: 'POST',
      body: JSON.stringify({ question }),
    })
  },

  async getMyQa(): Promise<QaItem[]> {
    return apiFetch<QaItem[]>('/api/members/me/qa', { method: 'GET' })
  },

  async getMyOrderQa(): Promise<QaItem[]> {
    return apiFetch<QaItem[]>('/api/members/me/order-qa', { method: 'GET' })
  },

  async getOrderQa(orderNo: string): Promise<QaItem[]> {
    return apiFetch<QaItem[]>(`/api/orders/${orderNo}/qa`, { method: 'GET' })
  },

  async addOrderQuestion(orderNo: string, question: string): Promise<QaItem> {
    return apiFetch<QaItem>(`/api/orders/${orderNo}/qa`, {
      method: 'POST',
      body: JSON.stringify({ question }),
    })
  },
}
