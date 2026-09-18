import { apiFetch } from '@shared/api/httpClient';
import type { ResultAsset } from '../domain/types';

export const resultsApi = {
  listMine() {
    return apiFetch<ResultAsset[]>('/me/result-assets');
  },
};
