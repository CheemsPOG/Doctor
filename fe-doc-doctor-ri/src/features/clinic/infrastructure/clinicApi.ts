import { apiFetch } from '@shared/api/httpClient';

export type QueueItem = {
  id: number;
  appointmentId: number;
  queueNumber: string;
  priority: number;
  status: string;
  assignedRoomId: number;
};

export type ClinicDayAppointment = {
  id: number;
  appointmentCode: string;
  patientId: number;
  doctorId: number;
  serviceId: number;
  startAt: string;
  endAt: string;
  status: string;
  reason: string;
  checkedIn: boolean;
  source: string;
};

export type CheckInResult = {
  queueId: number;
  queueNumber: string;
  status: string;
  priority: number;
};

export type ReceptionCreatePayload = {
  patientId: number;
  serviceId: number;
  doctorId: number;
  startAt: string;
  reason: string;
  walkIn?: boolean;
};

export const clinicApi = {
  queue(date: string) {
    return apiFetch<QueueItem[]>(`/clinic/queue?date=${encodeURIComponent(date)}`);
  },

  appointmentsByDate(date: string) {
    return apiFetch<ClinicDayAppointment[]>(
      `/clinic/appointments?date=${encodeURIComponent(date)}`,
    );
  },

  createReception(payload: ReceptionCreatePayload) {
    const { walkIn, ...body } = payload;
    const q = walkIn ? '?walkIn=true' : '';
    return apiFetch(`/reception/appointments${q}`, {
      method: 'POST',
      body: JSON.stringify(body),
      headers: { 'Idempotency-Key': crypto.randomUUID() },
    });
  },

  checkIn(appointmentId: string, medicalUrgent = false) {
    return apiFetch<CheckInResult>(`/appointments/${appointmentId}/check-in`, {
      method: 'POST',
      body: JSON.stringify({ medicalUrgent }),
    });
  },

  assignRoom(appointmentId: string, roomId: number) {
    return apiFetch(`/appointments/${appointmentId}/assign-room`, {
      method: 'POST',
      body: JSON.stringify({ roomId }),
    });
  },

  markNoShow(appointmentId: string) {
    return apiFetch(`/appointments/${appointmentId}/mark-no-show`, { method: 'POST' });
  },

  callQueue(queueId: number) {
    return apiFetch(`/queue/${queueId}/call`, { method: 'POST' });
  },

  startQueue(queueId: number) {
    return apiFetch(`/queue/${queueId}/start`, { method: 'POST' });
  },

  completeQueue(queueId: number) {
    return apiFetch(`/queue/${queueId}/complete`, { method: 'POST' });
  },

  reprioritize(queueId: number, priority: number, reason: string) {
    return apiFetch(`/queue/${queueId}/reprioritize`, {
      method: 'POST',
      body: JSON.stringify({ priority, reason }),
    });
  },

  rooms() {
    return apiFetch<{ id: number; code: string; name: string; roomType: string }[]>(
      '/clinic/rooms',
    );
  },
};
