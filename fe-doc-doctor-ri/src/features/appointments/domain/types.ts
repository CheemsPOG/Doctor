export type AppointmentStatus =
  | 'HELD'
  | 'CONFIRMED'
  | 'ATTENDANCE_CONFIRMED'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'NO_SHOW'
  | 'PENDING';

export type Appointment = {
  id: string;
  appointmentCode?: string;
  patientId: string;
  serviceId: string;
  doctorId: string;
  startAt: string;
  endAt?: string;
  reason?: string;
  status: AppointmentStatus;
  lateCancel?: boolean;
  serviceName?: string;
  doctorName?: string;
};

export type CreateAppointmentPayload = {
  patientId: string;
  serviceId: string;
  doctorId: string;
  startAt: string;
  reason: string;
};

export type RescheduleAppointmentPayload = {
  startAt: string;
  doctorId?: string;
};

export type TimeSlot = {
  slotKey: string;
  startAt: string;
  endAt: string;
  available: boolean;
};

export type AvailabilityResponse = {
  date: string;
  slots: TimeSlot[];
};

type AppointmentDto = {
  id: number | string;
  appointmentCode?: string;
  patientId: number | string;
  serviceId: number | string;
  doctorId: number | string;
  startAt: string;
  endAt?: string;
  reason?: string | null;
  status: AppointmentStatus;
  lateCancel?: boolean;
};

export function mapAppointment(dto: AppointmentDto): Appointment {
  return {
    id: String(dto.id),
    appointmentCode: dto.appointmentCode,
    patientId: String(dto.patientId),
    serviceId: String(dto.serviceId),
    doctorId: String(dto.doctorId),
    startAt: dto.startAt,
    endAt: dto.endAt,
    reason: dto.reason ?? undefined,
    status: dto.status,
    lateCancel: dto.lateCancel,
  };
}
