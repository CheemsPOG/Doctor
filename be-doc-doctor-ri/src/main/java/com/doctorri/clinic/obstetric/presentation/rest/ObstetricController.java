package com.doctorri.clinic.obstetric.presentation.rest;

import com.doctorri.clinic.obstetric.application.dto.CreatePregnancyRequest;
import com.doctorri.clinic.obstetric.application.dto.CreatePregnancyVisitRequest;
import com.doctorri.clinic.obstetric.application.dto.PregnancyResponse;
import com.doctorri.clinic.obstetric.application.dto.PregnancyVisitResponse;
import com.doctorri.clinic.obstetric.application.usecase.ObstetricApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/obstetric/pregnancies")
public class ObstetricController {

  private final ObstetricApplicationService obstetricService;

  public ObstetricController(ObstetricApplicationService obstetricService) {
    this.obstetricService = obstetricService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PregnancyResponse create(@Valid @RequestBody CreatePregnancyRequest request) {
    return obstetricService.create(request);
  }

  @GetMapping
  public List<PregnancyResponse> list(@RequestParam(required = false) Long patientId) {
    return obstetricService.list(patientId);
  }

  @PostMapping("/{id}/visits")
  @ResponseStatus(HttpStatus.CREATED)
  public PregnancyVisitResponse addVisit(@PathVariable Long id, @Valid @RequestBody CreatePregnancyVisitRequest request) {
    return obstetricService.addVisit(id, request);
  }

  @GetMapping("/{id}/visits")
  public List<PregnancyVisitResponse> visits(@PathVariable Long id) {
    return obstetricService.listVisits(id);
  }
}
