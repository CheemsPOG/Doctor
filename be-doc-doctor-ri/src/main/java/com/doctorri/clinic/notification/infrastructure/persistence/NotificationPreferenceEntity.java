package com.doctorri.clinic.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "notification_type", nullable = false)
  private String notificationType;

  @Column(name = "in_app_enabled", nullable = false)
  private boolean inAppEnabled = true;

  @Column(name = "email_enabled", nullable = false)
  private boolean emailEnabled = true;

  @Column(name = "sms_enabled", nullable = false)
  private boolean smsEnabled;

  @Column(name = "push_enabled", nullable = false)
  private boolean pushEnabled = true;

  @Column(name = "quiet_hours_start")
  private LocalTime quietHoursStart;

  @Column(name = "quiet_hours_end")
  private LocalTime quietHoursEnd;

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(String notificationType) {
    this.notificationType = notificationType;
  }

  public boolean isInAppEnabled() {
    return inAppEnabled;
  }

  public void setInAppEnabled(boolean inAppEnabled) {
    this.inAppEnabled = inAppEnabled;
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public void setEmailEnabled(boolean emailEnabled) {
    this.emailEnabled = emailEnabled;
  }

  public boolean isSmsEnabled() {
    return smsEnabled;
  }

  public void setSmsEnabled(boolean smsEnabled) {
    this.smsEnabled = smsEnabled;
  }

  public boolean isPushEnabled() {
    return pushEnabled;
  }

  public void setPushEnabled(boolean pushEnabled) {
    this.pushEnabled = pushEnabled;
  }

  public LocalTime getQuietHoursStart() {
    return quietHoursStart;
  }

  public void setQuietHoursStart(LocalTime quietHoursStart) {
    this.quietHoursStart = quietHoursStart;
  }

  public LocalTime getQuietHoursEnd() {
    return quietHoursEnd;
  }

  public void setQuietHoursEnd(LocalTime quietHoursEnd) {
    this.quietHoursEnd = quietHoursEnd;
  }
}
