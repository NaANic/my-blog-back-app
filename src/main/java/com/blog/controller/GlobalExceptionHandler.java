package com.blog.controller;

import com.blog.exception.PostNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String paramName = ex.getName();
    String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";

    // Для "undefined" возвращаем пустой массив (совместимость с фронтом)
    if ("undefined".equals(invalidValue) || "null".equals(invalidValue)) {
      log.debug("Ignoring undefined parameter: {}", paramName);
      return ResponseEntity.ok(Collections.emptyList());
    }

    log.warn("⚠️  Invalid parameter '{}' with value: {}", paramName, invalidValue);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body("Invalid parameter: " + paramName);
  }

  @ExceptionHandler(PostNotFoundException.class)
  public ResponseEntity<String> handlePostNotFound(PostNotFoundException ex) {
    log.warn("⚠️  {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ex.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    log.error("❌ Runtime error: {}", ex.getMessage(), ex);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Internal server error");
  }
}
