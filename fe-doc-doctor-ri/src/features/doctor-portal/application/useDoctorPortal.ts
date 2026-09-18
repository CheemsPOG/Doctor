import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { doctorPortalApi } from '../infrastructure/doctorPortalApi';

export function useDoctorSchedule(date: string) {
  return useQuery({
    queryKey: ['doctor-schedule', date],
    queryFn: () => doctorPortalApi.schedule(date),
    enabled: Boolean(date),
  });
}

export function useDoctorQueue() {
  return useQuery({
    queryKey: ['doctor-queue'],
    queryFn: () => doctorPortalApi.queue(),
    refetchInterval: 15_000,
  });
}

export function useStartExam() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (appointmentId: string) => doctorPortalApi.startExam(appointmentId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule'] });
      qc.invalidateQueries({ queryKey: ['doctor-queue'] });
    },
  });
}

export function useCompleteExam() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ appointmentId, summary }: { appointmentId: string; summary: string }) =>
      doctorPortalApi.completeExam(appointmentId, summary),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule'] });
      qc.invalidateQueries({ queryKey: ['doctor-queue'] });
    },
  });
}

export function useAddActivity() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      appointmentId,
      activityType,
      note,
    }: {
      appointmentId: string;
      activityType: string;
      note: string;
    }) => doctorPortalApi.addActivity(appointmentId, activityType, note),
    onSuccess: (_d, vars) => {
      qc.invalidateQueries({ queryKey: ['doctor-activities', vars.appointmentId] });
    },
  });
}

export function useActivities(appointmentId: string | null) {
  return useQuery({
    queryKey: ['doctor-activities', appointmentId],
    queryFn: () => doctorPortalApi.listActivities(appointmentId!),
    enabled: Boolean(appointmentId),
  });
}
