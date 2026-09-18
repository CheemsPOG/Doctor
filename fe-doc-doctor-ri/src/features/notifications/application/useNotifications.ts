import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationsApi } from '../infrastructure/notificationsApi';
import type { NotificationPreference } from '../domain/types';

export function useNotifications() {
  return useQuery({
    queryKey: ['me', 'notifications'],
    queryFn: () => notificationsApi.list(),
  });
}

export function useMarkNotificationRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => notificationsApi.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me', 'notifications'] }),
  });
}

export function useMarkAllNotificationsRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => notificationsApi.markAllRead(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me', 'notifications'] }),
  });
}

export function useNotificationPreferences() {
  return useQuery({
    queryKey: ['me', 'notification-preferences'],
    queryFn: () => notificationsApi.preferences(),
  });
}

export function useUpdateNotificationPreference() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: NotificationPreference) => notificationsApi.updatePreference(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me', 'notification-preferences'] }),
  });
}
