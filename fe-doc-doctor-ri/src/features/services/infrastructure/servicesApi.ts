import { apiFetch } from '@shared/api/httpClient';
import { mapService, type Service } from '../domain/types';

type ServiceDto = Parameters<typeof mapService>[0];

export const servicesApi = {
  async list(): Promise<Service[]> {
    const rows = await apiFetch<ServiceDto[]>('/services');
    return rows.map(mapService);
  },

  async get(id: string): Promise<Service> {
    const dto = await apiFetch<ServiceDto>(`/services/${id}`);
    return mapService(dto);
  },
};
