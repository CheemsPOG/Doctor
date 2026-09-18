package com.doctorri.clinic;

import com.doctorri.clinic.shared.infrastructure.config.BookingProperties;
import com.doctorri.clinic.shared.infrastructure.config.JwtProperties;
import com.doctorri.clinic.shared.infrastructure.config.NotificationProperties;
import com.doctorri.clinic.shared.infrastructure.config.PushProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    BookingProperties.class,
    JwtProperties.class,
    NotificationProperties.class,
    PushProperties.class
})
public class ClinicApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClinicApplication.class, args);
  }
}
