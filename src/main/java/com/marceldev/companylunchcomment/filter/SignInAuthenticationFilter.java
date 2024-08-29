package com.marceldev.companylunchcomment.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marceldev.companylunchcomment.component.AuthManager;
import com.marceldev.companylunchcomment.component.TokenProvider;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.TokenDto;
import com.marceldev.companylunchcomment.exception.member.SignInFailException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class SignInAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final AuthManager authManager;
  private final ObjectMapper objectMapper;
  private final HandlerExceptionResolver resolver;

  public SignInAuthenticationFilter(
      TokenProvider tokenProvider,
      AuthManager authManager,
      ObjectMapper objectMapper,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
  ) {
    this.tokenProvider = tokenProvider;
    this.authManager = authManager;
    this.objectMapper = objectMapper;
    this.resolver = resolver;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    SignInDto signIn = objectMapper.readValue(request.getInputStream(), SignInDto.class);
    String username = signIn.getEmail();
    String password = signIn.getPassword();

    try {
      // Retrieve token
      Authentication auth = authManager.authenticate(username, password);
      String role = auth.getAuthorities().stream().findFirst().toString();
      String token = tokenProvider.generateToken(username, role);

      // Give response token in body
      String jsonResponse = objectMapper.writeValueAsString(new TokenDto(token));
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(jsonResponse);

    } catch (Exception e) {
      // Let HandlerExceptionHandler handle this
      resolver.resolveException(request, response, null, new SignInFailException(e));
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !(request.getServletPath().equals("/members/signin") &&
        request.getMethod().equalsIgnoreCase("POST")); // POST signin 에만 이 필터가 적용됨
  }
}
