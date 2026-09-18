package com.doctorri.clinic.shared.infrastructure.config;

import com.doctorri.clinic.auth.infrastructure.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${doctorri.cors.allowed-origins}")
  private String allowedOrigins;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
    return new StringRedisTemplate(factory);
  }

  @Bean
  JavaMailSender javaMailSender(
      @Value("${spring.mail.host}") String host,
      @Value("${spring.mail.port}") int port) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    return sender;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/services/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/doctors/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/availability/**").permitAll()
            .requestMatchers("/api/v1/reception/**", "/api/v1/clinic/**")
            .hasAnyRole("RECEPTIONIST", "CLINIC_ADMIN", "SYSTEM_ADMIN")
            .requestMatchers(
                "/api/v1/appointments/*/check-in",
                "/api/v1/appointments/*/assign-room",
                "/api/v1/appointments/*/mark-no-show")
            .hasAnyRole("RECEPTIONIST", "CLINIC_ADMIN", "SYSTEM_ADMIN")
            .requestMatchers("/api/v1/queue/**")
            .hasAnyRole("RECEPTIONIST", "DOCTOR", "CLINIC_ADMIN", "SYSTEM_ADMIN", "NURSE")
            .requestMatchers("/api/v1/doctor/**").hasAnyRole("DOCTOR", "CLINIC_ADMIN", "SYSTEM_ADMIN")
            .requestMatchers("/api/v1/admin/**").hasAnyRole("CLINIC_ADMIN", "SYSTEM_ADMIN")
            .requestMatchers("/api/v1/**").authenticated()
            .anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
    boolean usePatterns = origins.stream().anyMatch(o -> o.contains("*"));
    if (usePatterns) {
      config.setAllowedOriginPatterns(origins);
    } else {
      config.setAllowedOrigins(origins);
    }
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
