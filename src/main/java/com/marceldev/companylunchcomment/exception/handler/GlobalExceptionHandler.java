package com.marceldev.companylunchcomment.exception.handler;

import com.marceldev.companylunchcomment.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<?> handler(
      CustomException e,
      HttpServletRequest request
  ) {
    log.error("CustomException, {}, {}", request.getRequestURI(), e.getMessage());

    HttpHeaders headers = new HttpHeaders();

    Map<String, String> body = new HashMap<>();
    body.put("errorType", e.getHttpStatusType());
    body.put("message", e.getMessage());

    return new ResponseEntity<>(body, headers, e.getHttpStatusCode());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(
      MethodArgumentNotValidException e,
      HttpServletRequest request
  ) {
    log.error("MethodArgumentNotValidException, {}, {}", request.getRequestURI(), e.getMessage());

    HttpHeaders headers = new HttpHeaders();

    Map<String, String> body = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(error -> {
      body.put(error.getField(), error.getDefaultMessage());
    });

    return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handlerAll(
      Exception e,
      HttpServletRequest request
  ) {
    log.error("Exception, {}, {}", request.getRequestURI(), e.getMessage());

    HttpHeaders headers = new HttpHeaders();

    Map<String, String> body = new HashMap<>();
    body.put("errorType", "unknown");
    body.put("message", "unknown");

    return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}