package com.marceldev.ourcompanylunch.exception.handler;

import com.marceldev.ourcompanylunch.dto.error.ErrorResponse;
import com.marceldev.ourcompanylunch.exception.common.CustomException;
import com.marceldev.ourcompanylunch.exception.member.SignInFailException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handler(
      CustomException e,
      HttpServletRequest request
  ) {
    log.error("CustomException, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    return ErrorResponse.serverError(9000, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handler(
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

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException e,
      HttpServletRequest request
  ) {
    log.error("MethodArgumentNotValidException, {}, {}, {}", request.getRequestURI(),
        e.getMessage(), String.valueOf(e.getCause()));

    StringBuilder sb = new StringBuilder();
    e.getBindingResult().getFieldErrors().forEach(error -> {
      sb.append(String.format("%s: %s\n", error.getField(), error.getDefaultMessage()));
    });
    return ErrorResponse.serverError(8000, sb.toString());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handlerAll(
      Exception e,
      HttpServletRequest request
  ) {
    log.error("Exception, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    return ErrorResponse.serverError(9000, "unknown");
  }
}