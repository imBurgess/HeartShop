import { apiFetch } from './http/client'

export interface WishlistItem {
  wishlistId: number
  productId: number
  productName: string
  productNameEn: string
  productCode: string
  price: number
  discountPrice: number
  productImage: string
  createdAt: string
}

export const wishlistService = {
  async getWishlist(): Promise<WishlistItem[]> {
    return apiFetch<WishlistItem[]>('/api/wishlist', { method: 'GET' })
  },

  async checkStatus(productId: number): Promise<boolean> {
    const res = await apiFetch<{ wishlisted: boolean }>(
      `/api/wishlist/${productId}/status`,
      { method: 'GET' }
    )
    return res.wishlisted
  },

  async add(productId: number): Promise<void> {
    await apiFetch(`/api/wishlist/${productId}`, { method: 'POST' })
  },

  async remove(productId: number): Promise<void> {
    await apiFetch(`/api/wishlist/${productId}`, { method: 'DELETE' })
  },

  async toggle(productId: number): Promise<boolean> {
    const isWishlisted = await this.checkStatus(productId)
    if (isWishlisted) {
      await this.remove(productId)
      return false
    } else {
      await this.add(productId)
      return true
    }
  }
}
