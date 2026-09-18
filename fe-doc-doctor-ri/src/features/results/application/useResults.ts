import { useQuery } from '@tanstack/react-query';
import { resultsApi } from '../infrastructure/resultsApi';

export function useMyResultAssets() {
  return useQuery({
    queryKey: ['me', 'result-assets'],
    queryFn: () => resultsApi.listMine(),
  });
}
