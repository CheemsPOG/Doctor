import { apiFetch } from '@shared/api/httpClient';

export type DoctorScheduleItem = {
  id: number;
  appointmentCode: string;
  patientId: number;
  startAt: string;
  endAt: string;
  status: string;
};

export type DoctorQueueItem = {
  id: number;
  appointmentId: number;
  queueNumber: string;
  status: string;
  priority: number;
};

export type EncounterActivity = {
  id: number;
  activityType: string;
  note: string;
};

export const doctorPortalApi = {
  schedule(date?: string) {
    const q = date ? `?date=${encodeURIComponent(date)}` : '';
    return apiFetch<DoctorScheduleItem[]>(`/doctor/me/schedule${q}`);
  },

  queue() {
    return apiFetch<DoctorQueueItem[]>('/doctor/me/queue');
  },

  startExam(appointmentId: string) {
    return apiFetch<{ encounterId: number; startedAt: string }>(
      `/appointments/${appointmentId}/start-exam`,
      { method: 'POST' },
    );
  },

  completeExam(appointmentId: string, summary: string) {
    return apiFetch<{ encounterId: number; endedAt: string }>(
      `/appointments/${appointmentId}/complete-exam`,
      { method: 'POST', body: JSON.stringify({ summary }) },
    );
  },

  addActivity(appointmentId: string, activityType: string, note: string) {
    return apiFetch(`/appointments/${appointmentId}/activities`, {
      method: 'POST',
      body: JSON.stringify({ activityType, note }),
    });
  },

  listActivities(appointmentId: string) {
    return apiFetch<EncounterActivity[]>(`/appointments/${appointmentId}/activities`);
  },
};
