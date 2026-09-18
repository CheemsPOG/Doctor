import { useEffect, useMemo, useRef } from "react";
import { Controller, useForm } from "react-hook-form";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getAuthSession } from "@shared/lib/authSession";
import { useServices } from "@features/services/application/useServices";
import { useDoctors } from "@features/doctors/application/useDoctors";
import {
  useAvailabilitySlots,
  useCreateAppointment,
} from "@features/appointments/application/useAppointments";
import { Button } from "@shared/ui/Button";
import { Input } from "@shared/ui/Input";
import { PageLoader } from "@shared/ui/Spinner";
import {
  formatDayPeriod,
  formatSlotDate,
  formatSlotTime,
  getDayPeriod,
  type DayPeriod,
} from "@shared/lib/formatSlotTime";
import styles from "./BookingPage.module.css";

const PERIOD_ORDER: DayPeriod[] = ["morning", "afternoon", "evening"];

type BookingFormValues = {
  serviceId: string;
  doctorId: string;
  date: string;
  startAt: string;
  reason: string;
};

export function BookingPage() {
  const session = getAuthSession();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedService = searchParams.get("serviceId") ?? "";
  const minDate = useMemo(() => new Date().toISOString().split("T")[0], []);

  const {
    control,
    register,
    handleSubmit,
    watch,
    setValue,
    clearErrors,
    formState: { errors, isSubmitting },
  } = useForm<BookingFormValues>({
    mode: "onBlur",
    reValidateMode: "onChange",
    defaultValues: {
      serviceId: preselectedService,
      doctorId: "",
      date: "",
      startAt: "",
      reason: "",
    },
  });

  useEffect(() => {
    if (preselectedService) {
      setValue("serviceId", preselectedService, { shouldValidate: true });
    }
  }, [preselectedService, setValue]);

  const serviceId = watch("serviceId");
  const doctorId = watch("doctorId");
  const date = watch("date");
  const startAt = watch("startAt");

  const { data: services, isLoading: loadingServices } = useServices();
  const { data: doctors, isLoading: loadingDoctors } = useDoctors();
  const availabilityParams =
    serviceId && doctorId && date ? { serviceId, doctorId, date } : null;
  const { data: availability, isLoading: loadingSlots } =
    useAvailabilitySlots(availabilityParams);
  const createAppointment = useCreateAppointment();

  const selectedService = services?.find((s) => s.id === serviceId);
  const selectedDoctor = doctors?.find((d) => d.id === doctorId);
  const availableSlots = availability?.slots.filter((s) => s.available) ?? [];
  const slotsByPeriod = useMemo(() => {
    const groups: Partial<Record<DayPeriod, typeof availableSlots>> = {};
    for (const slot of availableSlots) {
      const period = getDayPeriod(slot.startAt);
      if (!period) continue;
      (groups[period] ??= []).push(slot);
    }
    return PERIOD_ORDER.filter((p) => (groups[p]?.length ?? 0) > 0).map(
      (period) => ({ period, slots: groups[period]! }),
    );
  }, [availableSlots]);

  // Reset slot only when service / doctor / date actually change (not on every render)
  const upstreamKey = `${serviceId}|${doctorId}|${date}`;
  const prevUpstreamKey = useRef(upstreamKey);
  useEffect(() => {
    if (prevUpstreamKey.current === upstreamKey) return;
    prevUpstreamKey.current = upstreamKey;
    setValue("startAt", "");
    clearErrors("startAt");
  }, [upstreamKey, setValue, clearErrors]);

  const onSubmit = handleSubmit((values) => {
    if (!session?.patientId) return;

    createAppointment.mutate(
      {
        patientId: session.patientId,
        serviceId: values.serviceId,
        doctorId: values.doctorId,
        startAt: values.startAt,
        reason: values.reason.trim(),
      },
      {
        onSuccess: () => navigate("/patient/appointments"),
      },
    );
  });

  if (loadingServices || loadingDoctors) {
    return <PageLoader />;
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Đặt lịch khám</h1>
        <p className={styles.subtitle}>
          Điền một form bên dưới — mỗi mục sẽ được kiểm tra trước khi gửi.
        </p>
      </header>

      <form className={styles.form} onSubmit={onSubmit} noValidate>
        <div className={styles.field}>
          <label className={styles.label} htmlFor="serviceId">
            Dịch vụ <span aria-hidden>*</span>
          </label>
          <select
            id="serviceId"
            className={`${styles.select} ${errors.serviceId ? styles.invalid : ""}`}
            aria-invalid={Boolean(errors.serviceId)}
            {...register("serviceId", { required: "Vui lòng chọn dịch vụ" })}
          >
            <option value="">— Chọn dịch vụ —</option>
            {services?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
                {s.durationMinutes ? ` (${s.durationMinutes} phút)` : ""}
              </option>
            ))}
          </select>
          {errors.serviceId && (
            <p className={styles.fieldError} role="alert">
              {errors.serviceId.message}
            </p>
          )}
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="doctorId">
            Bác sĩ <span aria-hidden>*</span>
          </label>
          <select
            id="doctorId"
            className={`${styles.select} ${errors.doctorId ? styles.invalid : ""}`}
            aria-invalid={Boolean(errors.doctorId)}
            {...register("doctorId", { required: "Vui lòng chọn bác sĩ" })}
          >
            <option value="">— Chọn bác sĩ —</option>
            {doctors?.map((d) => (
              <option key={d.id} value={d.id}>
                {d.fullName}
                {d.specialty ? ` — ${d.specialty}` : ""}
              </option>
            ))}
          </select>
          {errors.doctorId && (
            <p className={styles.fieldError} role="alert">
              {errors.doctorId.message}
            </p>
          )}
        </div>

        <div className={styles.field}>
          <Input
            label="Ngày khám *"
            type="date"
            min={minDate}
            error={errors.date?.message}
            {...register("date", {
              required: "Vui lòng chọn ngày khám",
              validate: (v) =>
                !v || v >= minDate || "Không chọn ngày trong quá khứ",
            })}
          />
        </div>

        <div className={styles.field}>
          <span className={styles.label}>
            Khung giờ <span aria-hidden>*</span>
          </span>
          {!serviceId || !doctorId || !date ? (
            <p className={styles.hint}>
              Chọn dịch vụ, bác sĩ và ngày để xem khung giờ trống.
            </p>
          ) : loadingSlots ? (
            <PageLoader />
          ) : availableSlots.length === 0 ? (
            <p className={styles.empty}>
              Không có khung giờ trống trong ngày này.
            </p>
          ) : (
            <Controller
              name="startAt"
              control={control}
              rules={{ required: "Vui lòng chọn khung giờ" }}
              render={({ field }) => (
                <div className={styles.slotGroups} role="radiogroup" aria-label="Khung giờ">
                  {slotsByPeriod.map(({ period, slots }) => (
                    <section key={period} className={styles.slotGroup}>
                      <h3 className={styles.slotGroupTitle}>
                        {formatDayPeriod(period)}
                      </h3>
                      <div className={styles.slotGrid}>
                        {slots.map((slot) => {
                          const selected = field.value === slot.startAt;
                          return (
                            <button
                              key={slot.slotKey}
                              type="button"
                              role="radio"
                              aria-checked={selected}
                              aria-label={formatSlotTime(slot.startAt)}
                              className={`${styles.slot} ${selected ? styles.slotSelected : ""}`}
                              onClick={() => {
                                field.onChange(slot.startAt);
                                clearErrors("startAt");
                              }}
                            >
                              {formatSlotTime(slot.startAt)}
                            </button>
                          );
                        })}
                      </div>
                    </section>
                  ))}
                </div>
              )}
            />
          )}
          {errors.startAt && (
            <p className={styles.fieldError} role="alert">
              {errors.startAt.message}
            </p>
          )}
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="reason">
            Lý do khám / ghi chú <span aria-hidden>*</span>
          </label>
          <textarea
            id="reason"
            className={`${styles.textarea} ${errors.reason ? styles.invalid : ""}`}
            rows={4}
            placeholder="Ví dụ: Khám thai tuần 20, cảm giác mệt..."
            aria-invalid={Boolean(errors.reason)}
            {...register("reason", {
              required: "Vui lòng nhập lý do khám",
              validate: (v) =>
                v.trim().length >= 3 || "Lý do tối thiểu 3 ký tự",
            })}
          />
          {errors.reason && (
            <p className={styles.fieldError} role="alert">
              {errors.reason.message}
            </p>
          )}
        </div>

        {(selectedService || selectedDoctor || date || startAt) && (
          <aside className={styles.summaryBox} aria-live="polite">
            <h2 className={styles.summaryTitle}>Tóm tắt</h2>
            <dl className={styles.summary}>
              <div className={styles.summaryRow}>
                <dt>Dịch vụ</dt>
                <dd>{selectedService?.name ?? "—"}</dd>
              </div>
              <div className={styles.summaryRow}>
                <dt>Bác sĩ</dt>
                <dd>{selectedDoctor?.fullName ?? "—"}</dd>
              </div>
              <div className={styles.summaryRow}>
                <dt>Ngày khám</dt>
                <dd>
                  {startAt
                    ? formatSlotDate(startAt)
                    : date
                      ? formatSlotDate(`${date}T00:00:00`)
                      : "—"}
                </dd>
              </div>
              <div className={styles.summaryRow}>
                <dt>Giờ khám</dt>
                <dd>{startAt ? formatSlotTime(startAt) : "—"}</dd>
              </div>
            </dl>
          </aside>
        )}

        {createAppointment.isError && (
          <p className={styles.error} role="alert">
            {createAppointment.error.message}
          </p>
        )}

        <footer className={styles.footer}>
          <Button
            type="submit"
            fullWidth
            disabled={createAppointment.isPending || isSubmitting}
          >
            {createAppointment.isPending
              ? "Đang đặt lịch..."
              : "Xác nhận đặt lịch"}
          </Button>
        </footer>
      </form>
    </div>
  );
}
