export type PatientProfile = {
  patientId: number;
  userId: number;
  patientCode: string;
  fullName: string;
  dateOfBirth: string | null;
  gender: string | null;
  phone: string | null;
  email: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
};

export type UpdatePatientProfilePayload = {
  fullName: string;
  dateOfBirth?: string | null;
  gender?: string | null;
  phone?: string | null;
  email?: string | null;
  emergencyContactName?: string | null;
  emergencyContactPhone?: string | null;
};
