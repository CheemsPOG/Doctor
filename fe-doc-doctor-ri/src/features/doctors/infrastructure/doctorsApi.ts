import { apiFetch } from '@shared/api/httpClient';
import { mapDoctor, type Doctor } from '../domain/types';

type DoctorDto = Parameters<typeof mapDoctor>[0];

export const doctorsApi = {
  async list(): Promise<Doctor[]> {
    const rows = await apiFetch<DoctorDto[]>('/doctors');
    return rows.map(mapDoctor);
  },
};
