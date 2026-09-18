import { apiFetch } from '@shared/api/httpClient';
import {
  mapAppointment,
  type Appointment,
  type AvailabilityResponse,
  type CreateAppointmentPayload,
  type RescheduleAppointmentPayload,
} from '../domain/types';

type AppointmentDto = Parameters<typeof mapAppointment>[0];

export const appointmentsApi = {
  async list(patientId: string) {
    const rows = await apiFetch<AppointmentDto[]>(
      `/me/appointments?patientId=${encodeURIComponent(patientId)}`,
    );
    return rows.map(mapAppointment);
  },

  async create(payload: CreateAppointmentPayload) {
    const dto = await apiFetch<AppointmentDto>('/appointments', {
      method: 'POST',
      body: JSON.stringify({
        patientId: Number(payload.patientId),
        serviceId: Number(payload.serviceId),
        doctorId: Number(payload.doctorId),
        startAt: payload.startAt,
        reason: payload.reason,
      }),
      headers: {
        'Idempotency-Key': crypto.randomUUID(),
      },
    });
    return mapAppointment(dto);
  },

  async confirm(id: string) {
    const dto = await apiFetch<AppointmentDto>(`/appointments/${id}/confirm`, {
      method: 'POST',
    });
    return mapAppointment(dto);
  },

  async cancel(id: string) {
    const dto = await apiFetch<AppointmentDto>(`/appointments/${id}/cancel`, {
      method: 'POST',
    });
    return mapAppointment(dto);
  },

  async reschedule(id: string, payload: RescheduleAppointmentPayload) {
    const dto = await apiFetch<AppointmentDto>(`/appointments/${id}/reschedule`, {
      method: 'POST',
      body: JSON.stringify({
        startAt: payload.startAt,
        doctorId: payload.doctorId ? Number(payload.doctorId) : null,
      }),
    });
    return mapAppointment(dto);
  },

  getSlots(params: { serviceId: string; doctorId: string; date: string }) {
    const search = new URLSearchParams(params);
    return apiFetch<AvailabilityResponse>(`/availability/slots?${search.toString()}`);
  },
};

export type { Appointment };
