package com.doctorri.clinic.doctor.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorScheduleJpaRepository extends JpaRepository<DoctorScheduleEntity, Long> {

  List<DoctorScheduleEntity> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

  List<DoctorScheduleEntity> findByDoctorIdAndStatusOrderByDayOfWeekAscStartTimeAsc(
      Long doctorId, String status);

  @Query("""
      select s from DoctorScheduleEntity s
      where s.doctorId = :doctorId
        and s.status = 'ACTIVE'
        and s.dayOfWeek = :dayOfWeek
        and (s.effectiveFrom is null or s.effectiveFrom <= :date)
        and (s.effectiveTo is null or s.effectiveTo >= :date)
      order by s.startTime asc
      """)
  List<DoctorScheduleEntity> findActiveForDoctorOnDate(
      @Param("doctorId") Long doctorId,
      @Param("dayOfWeek") int dayOfWeek,
      @Param("date") LocalDate date);
}
