package com.doctorri.clinic.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_deliveries")
public class NotificationDeliveryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "notification_id", nullable = false)
  private Long notificationId;

  @Column(name = "recipient_user_id", nullable = false)
  private Long recipientUserId;

  @Column(nullable = false)
  private String channel;

  private String destination;

  @Column(nullable = false)
  private String status = "PENDING";

  private String provider;

  @Column(name = "provider_message_id")
  private String providerMessageId;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "next_retry_at")
  private LocalDateTime nextRetryAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "failed_at")
  private LocalDateTime failedAt;

  @Column(name = "error_code")
  private String errorCode;

  public Long getId() {
    return id;
  }

  public Long getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(Long notificationId) {
    this.notificationId = notificationId;
  }

  public Long getRecipientUserId() {
    return recipientUserId;
  }

  public void setRecipientUserId(Long recipientUserId) {
    this.recipientUserId = recipientUserId;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public void setProviderMessageId(String providerMessageId) {
    this.providerMessageId = providerMessageId;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public void setAttemptCount(int attemptCount) {
    this.attemptCount = attemptCount;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }

  public void setDeliveredAt(LocalDateTime deliveredAt) {
    this.deliveredAt = deliveredAt;
  }

  public void setFailedAt(LocalDateTime failedAt) {
    this.failedAt = failedAt;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
