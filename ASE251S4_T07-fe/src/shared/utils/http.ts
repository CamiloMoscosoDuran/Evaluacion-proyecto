const BACKEND_HOST = import.meta.env.VITE_API_BACKEND || 'localhost:8082';
const PROTOCOL = import.meta.env.VITE_API_PROTOCOL || 'http';
const BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/v1/api';

/**
 * Resuelve una URL reemplazando placeholders como {{API-BACKEND}}
 * o anteponiendo el host base si es una ruta relativa.
 * 
 * Ejemplos:
 *   resolveApiUrl("http://{{API-BACKEND}}/formula") -> "http://localhost:8082/v1/api/formula"
 *   resolveApiUrl("/formula") -> "http://localhost:8082/v1/api/formula"
 */
export function resolveApiUrl(urlOrPath: string): string {
  if (urlOrPath.includes('{{API-BACKEND}}') || urlOrPath.includes('{{API_BACKEND}}')) {
    return urlOrPath
      .split('{{API-BACKEND}}').join(BACKEND_HOST)
      .split('{{API_BACKEND}}').join(BACKEND_HOST);
  }

  if (urlOrPath.startsWith('http://') || urlOrPath.startsWith('https://')) {
    return urlOrPath;
  }

  const cleanBase = BASE_PATH.endsWith('/') ? BASE_PATH.slice(0, -1) : BASE_PATH;
  const cleanPath = urlOrPath.startsWith('/') ? urlOrPath : `/${urlOrPath}`;
  return `${PROTOCOL}://${BACKEND_HOST}${cleanBase}${cleanPath}`;
}

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
}

export async function request<T = unknown>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  let url = resolveApiUrl(endpoint);

  if (options.params) {
    const searchParams = new URLSearchParams();
    Object.entries(options.params).forEach(([key, val]) => {
      if (val !== undefined && val !== null) {
        searchParams.append(key, String(val));
      }
    });
    const qs = searchParams.toString();
    if (qs) {
      url += (url.includes('?') ? '&' : '?') + qs;
    }
  }

  const defaultHeaders: HeadersInit = {
    'Content-Type': 'application/json',
    'Accept': 'application/json, text/plain, */*',
  };

  const response = await fetch(url, {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (!response.ok) {
    let errorDetail = response.statusText;
    try {
      const errJson = await response.json();
      errorDetail = errJson.message || errJson.error || JSON.stringify(errJson);
    } catch {
      try {
        errorDetail = await response.text();
      } catch {
        // fallback to statusText
      }
    }
    throw new Error(`Error ${response.status} en ${url}: ${errorDetail}`);
  }

  if (response.status === 204) {
    return undefined as unknown as T;
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return (await response.json()) as T;
  }

  return (await response.text()) as unknown as T;
}

export const http = {
  get: <T>(url: string, options?: RequestOptions) =>
    request<T>(url, { ...options, method: 'GET' }),

  post: <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>(url, {
      ...options,
      method: 'POST',
      body: body instanceof FormData ? body : JSON.stringify(body),
    }),

  put: <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>(url, {
      ...options,
      method: 'PUT',
      body: body instanceof FormData ? body : JSON.stringify(body),
    }),

  patch: <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>(url, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    }),

  delete: <T>(url: string, options?: RequestOptions) =>
    request<T>(url, { ...options, method: 'DELETE' }),

  downloadBlob: async (url: string, filename: string): Promise<void> => {
    const fullUrl = resolveApiUrl(url);
    const resp = await fetch(fullUrl);
    if (!resp.ok) throw new Error(`Fallo al descargar archivo: ${resp.statusText}`);
    const blob = await resp.blob();
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },
};
