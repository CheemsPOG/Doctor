import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { clinicApi, type ReceptionCreatePayload } from '../infrastructure/clinicApi';

export function useClinicQueue(date: string) {
  return useQuery({
    queryKey: ['clinic-queue', date],
    queryFn: () => clinicApi.queue(date),
    enabled: Boolean(date),
    refetchInterval: 15_000,
  });
}

export function useClinicAppointments(date: string) {
  return useQuery({
    queryKey: ['clinic-appointments', date],
    queryFn: () => clinicApi.appointmentsByDate(date),
    enabled: Boolean(date),
    refetchInterval: 15_000,
  });
}

export function useClinicRooms() {
  return useQuery({
    queryKey: ['clinic-rooms'],
    queryFn: () => clinicApi.rooms(),
  });
}

function invalidateQueue(qc: ReturnType<typeof useQueryClient>) {
  qc.invalidateQueries({ queryKey: ['clinic-queue'] });
  qc.invalidateQueries({ queryKey: ['clinic-appointments'] });
}

export function useCheckIn() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      appointmentId,
      medicalUrgent,
    }: {
      appointmentId: string;
      medicalUrgent?: boolean;
    }) => clinicApi.checkIn(appointmentId, medicalUrgent),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useAssignRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ appointmentId, roomId }: { appointmentId: string; roomId: number }) =>
      clinicApi.assignRoom(appointmentId, roomId),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useMarkNoShow() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (appointmentId: string) => clinicApi.markNoShow(appointmentId),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useQueueCall() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (queueId: number) => clinicApi.callQueue(queueId),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useQueueStart() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (queueId: number) => clinicApi.startQueue(queueId),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useQueueComplete() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (queueId: number) => clinicApi.completeQueue(queueId),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useReprioritize() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      queueId,
      priority,
      reason,
    }: {
      queueId: number;
      priority: number;
      reason: string;
    }) => clinicApi.reprioritize(queueId, priority, reason),
    onSuccess: () => invalidateQueue(qc),
  });
}

export function useCreateReceptionAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: ReceptionCreatePayload) => clinicApi.createReception(payload),
    onSuccess: () => invalidateQueue(qc),
  });
}
