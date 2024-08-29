package com.marceldev.companylunchcomment.exception.handler;

import com.marceldev.companylunchcomment.dto.error.ErrorResponse;
import com.marceldev.companylunchcomment.exception.common.CustomException;
import com.marceldev.companylunchcomment.exception.member.SignInFailException;
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
    log.error("CustomException, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    HttpHeaders headers = new HttpHeaders();

    Map<String, String> body = new HashMap<>();
    body.put("message", e.getMessage());

    return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler
  public ResponseEntity<?> handler(
      SignInFailException e,
      HttpServletRequest request
  ) {
    log.error("SignInFailException, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    // 401을 내보내지 않음
    // 401은 요청 헤더에 authentication 을 요구하고, 응답에 challenge 를 보내는 경우에 사용한다는 스펙에 따름
    // https://stackoverflow.com/questions/11714485/restful-login-failure-return-401-or-custom-response
    return ErrorResponse.badRequest(1004, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(
      MethodArgumentNotValidException e,
      HttpServletRequest request
  ) {
    log.error("MethodArgumentNotValidException, {}, {}, {}", request.getRequestURI(),
        e.getMessage(), String.valueOf(e.getCause()));

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
    log.error("Exception, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    HttpHeaders headers = new HttpHeaders();

    Map<String, String> body = new HashMap<>();
    body.put("errorType", "unknown");
    body.put("message", "unknown");

    return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}