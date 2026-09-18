package com.doctorri.clinic.support;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers for MySQL + Redis. Starts once if Docker is available.
 */
public final class IntegrationContainers {

  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  public static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
      .withDatabaseName("db_clinic")
      .withUsername("test")
      .withPassword("test");

  @SuppressWarnings("resource")
  public static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
      .withExposedPorts(6379);

  private IntegrationContainers() {}

  public static boolean dockerAvailable() {
    try {
      DockerClientFactory.instance().client();
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }

  public static void ensureStarted() {
    if (!dockerAvailable()) {
      throw new IllegalStateException("Docker is required for integration tests");
    }
    if (STARTED.compareAndSet(false, true)) {
      MYSQL.start();
      REDIS.start();
    }
  }

  public static void register(DynamicPropertyRegistry registry) {
    ensureStarted();
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379).toString());
    registry.add("doctorri.push.provider", () -> "STUB");
    registry.add("doctorri.jwt.secret", () -> "integration-test-secret-key-32bytes!!");
    registry.add("spring.mail.host", () -> "127.0.0.1");
    registry.add("spring.mail.port", () -> "1025");
  }
}
