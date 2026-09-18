package com.doctorri.clinic.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.notification.infrastructure.channel.EmailChannelAdapter;
import com.doctorri.clinic.notification.infrastructure.channel.PushChannelAdapter;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventEntity;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationDeliveryJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationRecipientJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceJpaRepository;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.infrastructure.config.NotificationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxProcessorTest {

  @Mock OutboxEventJpaRepository outbox;
  @Mock NotificationJpaRepository notifications;
  @Mock NotificationRecipientJpaRepository recipients;
  @Mock NotificationDeliveryJpaRepository deliveries;
  @Mock PushDeviceJpaRepository devices;
  @Mock UserJpaRepository users;
  @Mock PatientJpaRepository patients;
  @Mock EmailChannelAdapter emailChannel;
  @Mock PushChannelAdapter pushChannel;

  NotificationOutboxProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new NotificationOutboxProcessor(
        outbox, notifications, recipients, deliveries, devices, users, patients,
        emailChannel, pushChannel,
        new NotificationProperties("EMAIL,PUSH", "noreply@doctorri.local", "24,2"),
        new ObjectMapper());
  }

  @Test
  void processesAppointmentCreatedIntoNotificationAndEmail() throws Exception {
    when(notifications.save(any(NotificationEntity.class))).thenAnswer(inv -> {
      NotificationEntity n = inv.getArgument(0);
      setId(n, 500L);
      return n;
    });

    PatientEntity patient = new PatientEntity();
    setId(patient, 100L);
    patient.setUserId(10L);
    when(patients.findById(100L)).thenReturn(Optional.of(patient));

    UserEntity user = new UserEntity();
    setId(user, 10L);
    user.setEmail("patient@example.com");
    when(users.findById(10L)).thenReturn(Optional.of(user));
    when(devices.findByUserIdAndActiveTrue(10L)).thenReturn(List.of());

    OutboxEventEntity event = new OutboxEventEntity();
    event.setAggregateType("APPOINTMENT");
    event.setAggregateId(1L);
    event.setEventType("APPOINTMENT_CREATED");
    event.setPayload("{\"appointmentId\":1,\"patientId\":100,\"code\":\"AP123\"}");
    event.setStatus("PENDING");

    processor.handle(event);

    verify(notifications, times(1)).save(any(NotificationEntity.class));
    verify(recipients, times(1)).save(any());
    verify(emailChannel, times(1)).send(eq("noreply@doctorri.local"), eq("patient@example.com"), anyString(), anyString());
    verify(deliveries, times(2)).save(any());
  }

  @Test
  void skipsWhenPatientMissing() throws Exception {
    when(patients.findById(100L)).thenReturn(Optional.empty());

    OutboxEventEntity event = new OutboxEventEntity();
    event.setAggregateType("APPOINTMENT");
    event.setAggregateId(1L);
    event.setEventType("APPOINTMENT_CREATED");
    event.setPayload("{\"appointmentId\":1,\"patientId\":100,\"code\":\"AP123\"}");
    event.setStatus("PENDING");

    processor.handle(event);

    verify(notifications, never()).save(any());
  }

  private static void setId(Object target, Long value) {
    try {
      var field = target.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
