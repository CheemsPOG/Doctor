import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { patientsApi } from '../infrastructure/patientsApi';
import type { UpdatePatientProfilePayload } from '../domain/types';

export function usePatientProfile() {
  return useQuery({
    queryKey: ['me', 'profile'],
    queryFn: () => patientsApi.getProfile(),
  });
}

export function useUpdatePatientProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdatePatientProfilePayload) => patientsApi.updateProfile(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['me', 'profile'] });
    },
  });
}
