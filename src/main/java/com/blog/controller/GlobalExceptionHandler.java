package com.blog.controller;

import com.blog.exception.PostNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PostNotFoundException.class)
  public ResponseEntity<Map<String, String>> handlePostNotFound(PostNotFoundException ex) {
    log.warn("Post not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationErrors(
      MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    log.warn("Validation errors: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String paramName = ex.getName();
    String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
    if ("undefined".equals(invalidValue) || "null".equals(invalidValue)) {
      log.debug("Ignoring undefined parameter: {}", paramName);
      return ResponseEntity.ok(Collections.emptyList());
    }
    log.warn("Invalid parameter '{}' with value: {}", paramName, invalidValue);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "Invalid parameter: " + paramName));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal server error"));
  }
}
