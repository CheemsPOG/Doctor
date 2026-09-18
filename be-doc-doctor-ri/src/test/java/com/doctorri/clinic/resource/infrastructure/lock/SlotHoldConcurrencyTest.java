package com.doctorri.clinic.resource.infrastructure.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.BookingProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class SlotHoldConcurrencyTest {

  @Mock StringRedisTemplate redis;
  @Mock ValueOperations<String, String> values;

  private SlotHoldService holds;
  private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

  @BeforeEach
  void setUp() {
    when(redis.opsForValue()).thenReturn(values);
    when(values.setIfAbsent(anyString(), anyString(), any(Duration.class)))
        .thenAnswer((Answer<Boolean>) inv -> store.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);
    holds = new SlotHoldService(redis, new BookingProperties(5, 30, 24, 15));
  }

  @Test
  void onlyOneThreadWinsSameSlotHold() throws Exception {
    LocalDateTime start = LocalDateTime.of(2026, 7, 25, 9, 0);
    int threads = 20;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch gate = new CountDownLatch(1);
    AtomicInteger wins = new AtomicInteger();
    AtomicInteger losses = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
      final int idx = i;
      pool.submit(() -> {
        ready.countDown();
        try {
          gate.await(5, TimeUnit.SECONDS);
          holds.hold(1L, start, "owner-" + idx);
          wins.incrementAndGet();
        } catch (DomainException ex) {
          assertEquals("SLOT_NOT_AVAILABLE", ex.getCode());
          losses.incrementAndGet();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    ready.await(5, TimeUnit.SECONDS);
    gate.countDown();
    pool.shutdown();
    pool.awaitTermination(15, TimeUnit.SECONDS);

    assertEquals(1, wins.get());
    assertEquals(threads - 1, losses.get());
  }

  @Test
  void secondHoldFailsImmediately() {
    LocalDateTime start = LocalDateTime.of(2026, 7, 25, 10, 0);
    holds.hold(2L, start, "a");
    assertThrows(DomainException.class, () -> holds.hold(2L, start, "b"));
  }
}
