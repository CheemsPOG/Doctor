import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { appointmentsApi } from '../infrastructure/appointmentsApi';
import type { CreateAppointmentPayload, RescheduleAppointmentPayload } from '../domain/types';

export function useAppointments(patientId: string | undefined) {
  return useQuery({
    queryKey: ['appointments', patientId],
    queryFn: () => appointmentsApi.list(patientId!),
    enabled: Boolean(patientId),
  });
}

export function useAvailabilitySlots(
  params: { serviceId: string; doctorId: string; date: string } | null,
) {
  return useQuery({
    queryKey: ['availability', params],
    queryFn: () => appointmentsApi.getSlots(params!),
    enabled: Boolean(params?.serviceId && params?.doctorId && params?.date),
  });
}

export function useCreateAppointment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateAppointmentPayload) => appointmentsApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

export function useConfirmAppointment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => appointmentsApi.confirm(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

export function useCancelAppointment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => appointmentsApi.cancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

export function useRescheduleAppointment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: RescheduleAppointmentPayload }) =>
      appointmentsApi.reschedule(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      queryClient.invalidateQueries({ queryKey: ['availability'] });
    },
  });
}
