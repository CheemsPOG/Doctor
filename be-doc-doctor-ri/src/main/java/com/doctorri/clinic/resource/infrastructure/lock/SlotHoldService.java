package com.doctorri.clinic.resource.infrastructure.lock;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.BookingProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SlotHoldService {
  private final StringRedisTemplate redis;
  private final BookingProperties props;

  public SlotHoldService(StringRedisTemplate redis, BookingProperties props) {
    this.redis = redis;
    this.props = props;
  }

  public String hold(Long doctorId, LocalDateTime start, String ownerKey) {
    String key = key(doctorId, start);
    Boolean ok = redis.opsForValue().setIfAbsent(key, ownerKey, Duration.ofMinutes(props.holdMinutes()));
    if (!Boolean.TRUE.equals(ok)) {
      throw new DomainException("SLOT_NOT_AVAILABLE", "Khung giờ vừa được người khác đặt.");
    }
    return key;
  }

  public void release(Long doctorId, LocalDateTime start) {
    redis.delete(key(doctorId, start));
  }

  private static String key(Long doctorId, LocalDateTime start) {
    return "slot:hold:" + doctorId + ":" + start.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
  }
}
