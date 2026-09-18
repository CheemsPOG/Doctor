import { apiFetch } from '@shared/api/httpClient';

export type AdminService = {
  id: number;
  code: string;
  name: string;
  category: string;
  description: string | null;
  durationMinutes: number;
  bufferBeforeMinutes: number;
  bufferAfterMinutes: number;
  holdRoomOnBooking: boolean;
  requiresUltrasound: boolean;
  bookingPolicy: string | null;
  status: string;
};

export type UpsertServicePayload = {
  code: string;
  name: string;
  category: string;
  description?: string;
  durationMinutes: number;
  bufferBeforeMinutes: number;
  bufferAfterMinutes: number;
  holdRoomOnBooking: boolean;
  requiresUltrasound: boolean;
  bookingPolicy?: string;
  status: string;
};

export type AdminDoctor = {
  id: number;
  clinicId: number | null;
  userId: number | null;
  doctorCode: string;
  fullName: string;
  specialty: string;
  bio: string | null;
  status: string;
  serviceIds: number[];
};

export type UpsertDoctorPayload = {
  doctorCode: string;
  fullName: string;
  specialty: string;
  bio?: string;
  clinicId?: number | null;
  userId?: number | null;
  status: string;
};

export type AdminSchedule = {
  id: number;
  doctorId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  status: string;
};

export type UpsertSchedulePayload = {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  status?: string;
};

export type AuditLog = {
  id: number;
  actorUserId?: number;
  action?: string;
  entityType?: string;
  entityId?: number;
  createdAt?: string;
};

export const adminApi = {
  listServices() {
    return apiFetch<AdminService[]>('/admin/services');
  },
  createService(payload: UpsertServicePayload) {
    return apiFetch<AdminService>('/admin/services', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateService(id: number, payload: UpsertServicePayload) {
    return apiFetch<AdminService>(`/admin/services/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deactivateService(id: number) {
    return apiFetch<AdminService>(`/admin/services/${id}`, { method: 'DELETE' });
  },

  listDoctors() {
    return apiFetch<AdminDoctor[]>('/admin/doctors');
  },
  createDoctor(payload: UpsertDoctorPayload) {
    return apiFetch<AdminDoctor>('/admin/doctors', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateDoctor(id: number, payload: UpsertDoctorPayload) {
    return apiFetch<AdminDoctor>(`/admin/doctors/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deactivateDoctor(id: number) {
    return apiFetch<AdminDoctor>(`/admin/doctors/${id}`, { method: 'DELETE' });
  },
  setDoctorServices(id: number, serviceIds: number[]) {
    return apiFetch<{ serviceIds: number[] }>(`/admin/doctors/${id}/services`, {
      method: 'PUT',
      body: JSON.stringify({ serviceIds }),
    });
  },

  listSchedules(doctorId: number) {
    return apiFetch<AdminSchedule[]>(`/admin/doctors/${doctorId}/schedules`);
  },
  createSchedule(doctorId: number, payload: UpsertSchedulePayload) {
    return apiFetch<AdminSchedule>(`/admin/doctors/${doctorId}/schedules`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateSchedule(scheduleId: number, payload: UpsertSchedulePayload) {
    return apiFetch<AdminSchedule>(`/admin/schedules/${scheduleId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deactivateSchedule(scheduleId: number) {
    return apiFetch<void>(`/admin/schedules/${scheduleId}`, { method: 'DELETE' });
  },

  rooms() {
    return apiFetch<{ id: number; code: string; name: string; roomType: string }[]>(
      '/admin/rooms',
    );
  },
  equipments() {
    return apiFetch<{ id: number; code: string; name: string; equipmentType: string }[]>(
      '/admin/equipments',
    );
  },
  auditLogs() {
    return apiFetch<AuditLog[]>('/admin/audit-logs');
  },
};
