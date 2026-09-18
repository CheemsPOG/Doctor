package com.doctorri.clinic.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_recipients")
public class NotificationRecipientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "notification_id", nullable = false)
  private Long notificationId;

  @Column(name = "recipient_user_id", nullable = false)
  private Long recipientUserId;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "is_archived", nullable = false)
  private boolean archived;

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

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public LocalDateTime getReadAt() {
    return readAt;
  }

  public void setReadAt(LocalDateTime readAt) {
    this.readAt = readAt;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }
}
