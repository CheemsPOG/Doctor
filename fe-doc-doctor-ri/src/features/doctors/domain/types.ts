export type Doctor = {
  id: string;
  fullName: string;
  specialty?: string;
  bio?: string;
  doctorCode?: string;
};

type DoctorDto = {
  id: number | string;
  fullName: string;
  specialty?: string;
  bio?: string;
  doctorCode?: string;
};

export function mapDoctor(dto: DoctorDto): Doctor {
  return {
    id: String(dto.id),
    fullName: dto.fullName,
    specialty: dto.specialty,
    bio: dto.bio,
    doctorCode: dto.doctorCode,
  };
}
