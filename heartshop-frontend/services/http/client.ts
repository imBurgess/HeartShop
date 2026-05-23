export const apiFetch = async <T>(url: string, opts: any = {}) => {
  let token = null;
  try {
    // 優先從 Nuxt 內文取 (主要為了 SSR)
    token = useCookie<string | null>("token").value;
  } catch (e) {
    // ignore
  }

  // 如果取不到，但在客戶端環境，直接解析 document.cookie
  if (!token && typeof document !== 'undefined') {
    const match = document.cookie.match(new RegExp('(^| )token=([^;]+)'));
    if (match) {
      const val = decodeURIComponent(match[2] || '');
      if (val !== 'null' && val !== 'undefined') {
        token = val;
      }
    }
  }

  const headers: Record<string, string> = { ...opts.headers };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await $fetch<{ code: string; data: T }>(url, {
    method: opts.method ?? "GET",
    query: opts.query,
    body: opts.body,
    headers: headers,
  });

  // 提取 ApiResponse 的 data 欄位
  return response.data;
};
