package com.doctorri.clinic.patient.presentation.rest;

import com.doctorri.clinic.patient.application.dto.PatientProfileResponse;
import com.doctorri.clinic.patient.application.dto.UpdatePatientProfileRequest;
import com.doctorri.clinic.patient.application.usecase.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/profile")
public class PatientProfileController {

  private final PatientProfileService profiles;

  public PatientProfileController(PatientProfileService profiles) {
    this.profiles = profiles;
  }

  @GetMapping
  public PatientProfileResponse get() {
    return profiles.getMine();
  }

  @PutMapping
  public PatientProfileResponse update(@Valid @RequestBody UpdatePatientProfileRequest request) {
    return profiles.updateMine(request);
  }
}
