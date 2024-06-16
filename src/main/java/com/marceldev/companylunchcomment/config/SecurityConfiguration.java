package com.marceldev.companylunchcomment.config;

import com.marceldev.companylunchcomment.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorization -> authorization
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                "/swagger-resources/**", "/webjars/**").permitAll() // Swagger UI
            .requestMatchers("/members/signup", "/members/signin",
                "/members/signup/send-verification-code").permitAll()
            // sse 부분을 authenticated로 연결해놓으면, sse가 terminate되는 시점에 SecurityContextHolder가 비워지며 access denied 에러를 냄
            // permitAll이지만 헤더에 Authorization 부분이 없으면 SecurityContextHolder가 채워지지 않아 sse 연결중 에러를 내긴 함
            // TODO: 다르게 처리할 방법이 있는지 확인
            .requestMatchers("/notifications/sse").permitAll()
            .requestMatchers("/notifications/**").authenticated()
            .requestMatchers("/members/**").authenticated()
            .requestMatchers("/diners/**").authenticated()
            .requestMatchers("/companies/**").authenticated()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}