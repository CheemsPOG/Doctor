import { apiFetch } from '@shared/api/httpClient';
import { mapAuthResponse, type AuthCredentials, type AuthResponse, type AuthResponseDto, type RegisterPayload } from '../domain/types';

export const authApi = {
  async login(payload: AuthCredentials): Promise<AuthResponse> {
    const dto = await apiFetch<AuthResponseDto>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return mapAuthResponse(dto);
  },

  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const dto = await apiFetch<AuthResponseDto>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return mapAuthResponse(dto);
  },

  logout(refreshToken?: string) {
    return apiFetch<void>('/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken: refreshToken ?? '' }),
    }).catch(() => undefined);
  },
};
