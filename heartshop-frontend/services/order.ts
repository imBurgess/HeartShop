import { apiFetch } from './http/client'

export interface PlaceOrderRequest {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  receiverNote?: string
  paymentMethod: string
  shippingMethod: string
  clientBackUrl: string
}

export interface PlaceOrderResponse {
  orderNo: string
  paymentUrl: string
  ecpayParams: Record<string, string>
}

export interface OrderItem {
  orderItemId: number
  orderId: number
  productId: number
  productCode: string
  productName: string
  productImage: string
  sizeName: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface Order {
  orderId: number
  orderNo: string
  totalAmount: number
  subtotalAmount: number
  shippingFee: number
  status: string
  paymentMethod: string
  shippingMethod: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  createdAt: string
  updatedAt: string
  items?: OrderItem[]
}

export const orderService = {
  /**
   * 建立訂單，取得綠界所需參數
   */
  async placeOrder(payload: PlaceOrderRequest) {
    try {
      const data = await apiFetch<PlaceOrderResponse>('/api/orders', {
        method: 'POST',
        body: payload
      })
      return data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '訂單建立失敗')
    }
  },

  /**
   * 根據訂單編號取得訂單詳情
   */
  async getOrder(orderNo: string) {
    try {
      const data = await apiFetch<Order>(`/api/orders/${orderNo}`, {
        method: 'GET'
      })
      return data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '找不到訂單資料')
    }
  },

  /**
   * 取得目前登入會員的所有訂單
   */
  async getMemberOrders() {
    try {
      const data = await apiFetch<Order[]>('/api/orders/member', { method: 'GET' })
      return data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '載入訂單列表失敗')
    }
  },

  /**
   * 取消訂單（僅限 pending / FAILED 狀態）
   */
  async cancelOrder(orderNo: string) {
    try {
      await apiFetch(`/api/orders/${orderNo}/cancel`, { method: 'PATCH' })
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '取消訂單失敗')
    }
  },

  /**
   * 重新付款：取得綠界表單參數（僅限 pending 狀態）
   */
  async repayOrder(orderNo: string) {
    try {
      const data = await apiFetch<PlaceOrderResponse>(`/api/orders/${orderNo}/repay`, { method: 'POST' })
      return data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '取得付款資訊失敗')
    }
  }
}
