package com.doctorri.clinic.shared.presentation.advice;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.presentation.rest.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), traceId(request), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(FieldError::getDefaultMessage)
        .orElse("Dữ liệu không hợp lệ");
    return ResponseEntity.badRequest()
        .body(new ApiErrorResponse("VALIDATION_ERROR", message, traceId(request), null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse("INTERNAL_ERROR", "Đã xảy ra lỗi hệ thống.", traceId(request), null));
  }

  private static String traceId(HttpServletRequest request) {
    String existing = request.getHeader("X-Trace-Id");
    return existing != null && !existing.isBlank() ? existing : UUID.randomUUID().toString();
  }
}
