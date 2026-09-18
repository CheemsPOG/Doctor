import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  adminApi,
  type UpsertDoctorPayload,
  type UpsertSchedulePayload,
  type UpsertServicePayload,
} from '../infrastructure/adminApi';

export function useAdminServices() {
  return useQuery({ queryKey: ['admin-services'], queryFn: () => adminApi.listServices() });
}

export function useUpsertService() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (args: { id?: number; payload: UpsertServicePayload }) =>
      args.id
        ? adminApi.updateService(args.id, args.payload)
        : adminApi.createService(args.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-services'] }),
  });
}

export function useDeactivateService() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => adminApi.deactivateService(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-services'] }),
  });
}

export function useAdminDoctors() {
  return useQuery({ queryKey: ['admin-doctors'], queryFn: () => adminApi.listDoctors() });
}

export function useUpsertDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (args: { id?: number; payload: UpsertDoctorPayload }) =>
      args.id
        ? adminApi.updateDoctor(args.id, args.payload)
        : adminApi.createDoctor(args.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-doctors'] }),
  });
}

export function useDeactivateDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => adminApi.deactivateDoctor(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-doctors'] }),
  });
}

export function useSetDoctorServices() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, serviceIds }: { id: number; serviceIds: number[] }) =>
      adminApi.setDoctorServices(id, serviceIds),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-doctors'] }),
  });
}

export function useAdminSchedules(doctorId: number | null) {
  return useQuery({
    queryKey: ['admin-schedules', doctorId],
    queryFn: () => adminApi.listSchedules(doctorId!),
    enabled: doctorId != null,
  });
}

export function useCreateSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ doctorId, payload }: { doctorId: number; payload: UpsertSchedulePayload }) =>
      adminApi.createSchedule(doctorId, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-schedules'] }),
  });
}

export function useDeactivateSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (scheduleId: number) => adminApi.deactivateSchedule(scheduleId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-schedules'] }),
  });
}

export function useAdminRooms() {
  return useQuery({ queryKey: ['admin-rooms'], queryFn: () => adminApi.rooms() });
}

export function useAdminEquipments() {
  return useQuery({ queryKey: ['admin-equipments'], queryFn: () => adminApi.equipments() });
}

export function useAdminAuditLogs() {
  return useQuery({ queryKey: ['admin-audit'], queryFn: () => adminApi.auditLogs() });
}
