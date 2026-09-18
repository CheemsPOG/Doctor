package com.doctorri.clinic.shared.infrastructure.seed;

import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.result.infrastructure.persistence.ClinicalResultAssetEntity;
import com.doctorri.clinic.result.infrastructure.persistence.ClinicalResultAssetJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class DemoPatientSeeder implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(DemoPatientSeeder.class);
  private static final String DEMO_PATIENT_EMAIL = "mebau@doctorri.local";
  private final UserJpaRepository users;
  private final PatientJpaRepository patients;
  private final DoctorJpaRepository doctors;
  private final ServiceJpaRepository services;
  private final ClinicalResultAssetJpaRepository resultAssets;
  private final PasswordEncoder passwordEncoder;

  public DemoPatientSeeder(UserJpaRepository users, PatientJpaRepository patients,
      DoctorJpaRepository doctors, ServiceJpaRepository services,
      ClinicalResultAssetJpaRepository resultAssets, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.patients = patients;
    this.doctors = doctors;
    this.services = services;
    this.resultAssets = resultAssets;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedUser(DEMO_PATIENT_EMAIL, "Nguyễn Thị Hoa", "0901000001", UserRole.PATIENT, true);
    seedUser("receptionist@doctorri.local", "Lễ tân Mai", "0901000002", UserRole.RECEPTIONIST, false);
    Long doctorUserId = seedUser("doctor.ri@doctorri.local", "Bác sĩ Ri", "0901000003", UserRole.DOCTOR, false);
    seedUser("admin@doctorri.local", "Admin Clinic", "0901000004", UserRole.CLINIC_ADMIN, false);
    doctors.findByDoctorCode("DR-RI").ifPresent(d -> {
      if (d.getUserId() == null) {
        d.setUserId(doctorUserId);
      }
    });
    seedDemoResultAssets();
  }

  private Long seedUser(String email, String name, String phone, UserRole role, boolean asPatient) {
    return users.findByEmailIgnoreCase(email).map(UserEntity::getId).orElseGet(() -> {
      UserEntity user = new UserEntity();
      user.setEmail(email);
      user.setPhone(phone);
      user.setFullName(name);
      user.setPasswordHash(passwordEncoder.encode("Password123!"));
      user.setRole(role.name());
      user.setStatus("ACTIVE");
      users.save(user);
      if (asPatient) {
        PatientEntity patient = new PatientEntity();
        patient.setUserId(user.getId());
        patient.setPatientCode("PT" + String.format("%06d", user.getId()));
        patient.setFullName(name);
        patient.setPhone(phone);
        patient.setEmail(email);
        patients.save(patient);
      }
      log.info("Seeded {} / Password123! role={}", email, role);
      return user.getId();
    });
  }

  private void seedDemoResultAssets() {
    var patient = users.findByEmailIgnoreCase(DEMO_PATIENT_EMAIL)
        .flatMap(u -> patients.findByUserId(u.getId()))
        .orElse(null);
    if (patient == null) {
      return;
    }
    if (!resultAssets.findByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), "ACTIVE").isEmpty()) {
      return;
    }
    Long serviceId = services.findByCode("RESULT_CONSULT").map(s -> s.getId()).orElse(null);

    ClinicalResultAssetEntity image = new ClinicalResultAssetEntity();
    image.setPatientId(patient.getId());
    image.setServiceId(serviceId);
    image.setMediaType("IMAGE");
    image.setTitle("Ảnh siêu âm minh họa");
    image.setCaption("Hình minh họa kết quả siêu âm (demo). Production sẽ lấy từ MinIO.");
    image.setUrl("https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80");
    image.setThumbnailUrl("https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=400&q=60");
    image.setStatus("ACTIVE");
    resultAssets.save(image);

    ClinicalResultAssetEntity video = new ClinicalResultAssetEntity();
    video.setPatientId(patient.getId());
    video.setServiceId(serviceId);
    video.setMediaType("VIDEO");
    video.setTitle("Clip giải thích kết quả (demo)");
    video.setCaption("Video minh họa phần tư vấn kết quả. Thay bằng file phòng khám khi tích hợp lưu trữ.");
    video.setUrl("https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4");
    video.setStatus("ACTIVE");
    resultAssets.save(video);
    log.info("Seeded demo clinical result assets for {}", DEMO_PATIENT_EMAIL);
  }
}
