import { apiFetch } from '@shared/api/httpClient';
import type { NotificationItem, NotificationPreference } from '../domain/types';

export const notificationsApi = {
  list() {
    return apiFetch<NotificationItem[]>('/me/notifications');
  },

  markRead(id: number) {
    return apiFetch<void>(`/me/notifications/${id}/read`, { method: 'PATCH' });
  },

  markAllRead() {
    return apiFetch<void>('/me/notifications/read-all', { method: 'PATCH' });
  },

  preferences() {
    return apiFetch<NotificationPreference[]>('/me/notification-preferences');
  },

  updatePreference(payload: NotificationPreference) {
    return apiFetch<NotificationPreference>('/me/notification-preferences', {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
};
