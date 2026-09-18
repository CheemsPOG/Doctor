import { env } from '@shared/config/env';
import { clearAuthSession, getAuthSession } from '@shared/lib/authSession';

export type ApiErrorBody = {
  code: string;
  message: string;
  traceId?: string;
  details?: unknown;
};

export async function apiFetch<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const session = getAuthSession();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init?.headers as Record<string, string> | undefined),
  };

  if (session?.accessToken) {
    headers.Authorization = `Bearer ${session.accessToken}`;
  }

  try {
    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      ...init,
      headers,
    });

    if (response.status === 401) {
      clearAuthSession();
    }

    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
      throw new Error(body?.message ?? `HTTP ${response.status}`);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json() as Promise<T>;
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error(
        'Không kết nối được máy chủ API. Kiểm tra BE đang chạy tại :8080 và Vite proxy /api.',
      );
    }
    throw err;
  }
}
