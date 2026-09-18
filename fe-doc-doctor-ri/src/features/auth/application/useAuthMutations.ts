import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { setAuthSession } from '@shared/lib/authSession';
import { homePathForRole } from '@shared/lib/roles';
import { authApi } from '../infrastructure/authApi';
import type { AuthCredentials, RegisterPayload } from '../domain/types';

export function useLogin() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (payload: AuthCredentials) => authApi.login(payload),
    onSuccess: (data) => {
      setAuthSession(data);
      navigate(homePathForRole(data.role));
    },
  });
}

export function useRegister() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (payload: RegisterPayload) => authApi.register(payload),
    onSuccess: (data) => {
      setAuthSession(data);
      navigate(homePathForRole(data.role));
    },
  });
}
