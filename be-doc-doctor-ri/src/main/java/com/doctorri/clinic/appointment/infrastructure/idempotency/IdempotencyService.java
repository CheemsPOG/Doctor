package com.doctorri.clinic.appointment.infrastructure.idempotency;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

  private static final Duration TTL = Duration.ofHours(24);

  private final StringRedisTemplate redis;

  public IdempotencyService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public Optional<Long> findExisting(Long userId, String key) {
    if (key == null || key.isBlank()) {
      return Optional.empty();
    }
    String value = redis.opsForValue().get(redisKey(userId, key));
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Long.parseLong(value));
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }

  public Long executeOnce(Long userId, String key, Supplier<Long> createAppointmentId) {
    if (key == null || key.isBlank()) {
      return createAppointmentId.get();
    }
    Optional<Long> existing = findExisting(userId, key);
    if (existing.isPresent()) {
      return existing.get();
    }
    String redisKey = redisKey(userId, key);
    Boolean claimed = redis.opsForValue().setIfAbsent(redisKey, "PENDING", TTL);
    if (!Boolean.TRUE.equals(claimed)) {
      // Another request in flight or already stored — wait briefly for result
      for (int i = 0; i < 20; i++) {
        Optional<Long> again = findExisting(userId, key);
        if (again.isPresent()) {
          return again.get();
        }
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new DomainException("IDEMPOTENCY_CONFLICT", "Yêu cầu trùng đang được xử lý.");
        }
      }
      throw new DomainException("IDEMPOTENCY_CONFLICT", "Yêu cầu trùng đang được xử lý.");
    }
    try {
      Long id = createAppointmentId.get();
      redis.opsForValue().set(redisKey, String.valueOf(id), TTL);
      return id;
    } catch (RuntimeException ex) {
      redis.delete(redisKey);
      throw ex;
    }
  }

  private static String redisKey(Long userId, String key) {
    return "idempotency:appointment:" + userId + ":" + key.trim();
  }
}
