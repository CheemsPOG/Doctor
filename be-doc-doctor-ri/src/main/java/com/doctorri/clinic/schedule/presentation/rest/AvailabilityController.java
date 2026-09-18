package com.doctorri.clinic.schedule.presentation.rest;

import com.doctorri.clinic.schedule.application.usecase.SlotAvailabilityService;
import com.doctorri.clinic.schedule.application.usecase.SlotAvailabilityService.SlotDayResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

  private final SlotAvailabilityService slotAvailabilityService;

  public AvailabilityController(SlotAvailabilityService slotAvailabilityService) {
    this.slotAvailabilityService = slotAvailabilityService;
  }

  @GetMapping("/slots")
  public SlotDayResponse slots(
      @RequestParam Long serviceId,
      @RequestParam Long doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return slotAvailabilityService.listSlots(serviceId, doctorId, date);
  }
}
