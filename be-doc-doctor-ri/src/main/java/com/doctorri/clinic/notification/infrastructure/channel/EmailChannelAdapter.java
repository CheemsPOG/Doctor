package com.doctorri.clinic.notification.infrastructure.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelAdapter {

  private static final Logger log = LoggerFactory.getLogger(EmailChannelAdapter.class);
  private final JavaMailSender mailSender;

  public EmailChannelAdapter(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void send(String from, String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
      log.info("Email sent to {}", to);
    } catch (Exception ex) {
      log.warn("Mail server unavailable, logging email instead. to={} subject={} body={}", to, subject, body);
    }
  }
}
