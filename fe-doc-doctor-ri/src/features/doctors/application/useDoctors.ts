import { useQuery } from '@tanstack/react-query';
import { doctorsApi } from '../infrastructure/doctorsApi';

export function useDoctors() {
  return useQuery({
    queryKey: ['doctors'],
    queryFn: () => doctorsApi.list(),
  });
}
