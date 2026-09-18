import { useQuery } from '@tanstack/react-query';
import { servicesApi } from '../infrastructure/servicesApi';

export function useServices() {
  return useQuery({
    queryKey: ['services'],
    queryFn: () => servicesApi.list(),
  });
}
