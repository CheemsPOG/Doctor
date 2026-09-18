package com.doctorri.clinic.shared.infrastructure.seed;

import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds RECEPTIONIST / DOCTOR / CLINIC_ADMIN demo accounts and links DR-RI to its user. */
@Component
@Order(10)
public class DemoStaffSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoStaffSeeder.class);
  private static final String DEFAULT_PASSWORD = "Password123!";

  private final UserJpaRepository users;
  private final DoctorJpaRepository doctors;
  private final PasswordEncoder passwordEncoder;

  public DemoStaffSeeder(UserJpaRepository users, DoctorJpaRepository doctors, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.doctors = doctors;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    UserEntity receptionist = seedUser("receptionist@doctorri.local", "Lễ tân Doctor Ri", "RECEPTIONIST");
    UserEntity doctorUser = seedUser("doctor.ri@doctorri.local", "Bác sĩ Ri", "DOCTOR");
    seedUser("admin@doctorri.local", "Quản trị Doctor Ri", "CLINIC_ADMIN");

    doctors.findAll().stream()
        .filter(d -> "DR-RI".equals(d.getDoctorCode()))
        .findFirst()
        .ifPresent(doctor -> linkDoctorUser(doctor, doctorUser));
  }

  private UserEntity seedUser(String email, String fullName, String role) {
    return users.findByEmailIgnoreCase(email).orElseGet(() -> {
      UserEntity user = new UserEntity();
      user.setEmail(email);
      user.setFullName(fullName);
      user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
      user.setRole(role);
      user.setStatus("ACTIVE");
      users.save(user);
      log.info("Seeded demo staff {} ({}) / {}", email, role, DEFAULT_PASSWORD);
      return user;
    });
  }

  private void linkDoctorUser(DoctorEntity doctor, UserEntity doctorUser) {
    if (doctor.getUserId() == null) {
      doctor.setUserId(doctorUser.getId());
      doctors.save(doctor);
      log.info("Linked doctor {} to user {}", doctor.getDoctorCode(), doctorUser.getEmail());
    }
  }
}
