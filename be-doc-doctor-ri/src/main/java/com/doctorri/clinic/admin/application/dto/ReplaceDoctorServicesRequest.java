package com.doctorri.clinic.admin.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceDoctorServicesRequest(@NotNull List<Long> serviceIds) {}
