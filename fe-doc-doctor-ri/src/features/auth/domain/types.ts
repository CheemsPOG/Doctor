export type AuthCredentials = {
  email: string;
  password: string;
};

export type RegisterPayload = AuthCredentials & {
  fullName: string;
  phone: string;
};

/** Raw BE payload (numeric ids). */
export type AuthResponseDto = {
  accessToken: string;
  refreshToken?: string;
  expiresIn?: number;
  userId: number | string;
  patientId?: number | string | null;
  doctorId?: number | string | null;
  fullName: string;
  email: string;
  role: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken?: string;
  userId: string;
  patientId: string;
  doctorId: string;
  fullName: string;
  email: string;
  role: string;
};

export function mapAuthResponse(dto: AuthResponseDto): AuthResponse {
  return {
    accessToken: dto.accessToken,
    refreshToken: dto.refreshToken,
    userId: String(dto.userId),
    patientId: dto.patientId != null ? String(dto.patientId) : '',
    doctorId: dto.doctorId != null ? String(dto.doctorId) : '',
    fullName: dto.fullName,
    email: dto.email,
    role: dto.role,
  };
}
