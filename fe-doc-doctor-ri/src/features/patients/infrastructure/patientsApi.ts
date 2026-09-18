import { apiFetch } from '@shared/api/httpClient';
import type { PatientProfile, UpdatePatientProfilePayload } from '../domain/types';

export const patientsApi = {
  getProfile() {
    return apiFetch<PatientProfile>('/me/profile');
  },

  updateProfile(payload: UpdatePatientProfilePayload) {
    return apiFetch<PatientProfile>('/me/profile', {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
};
