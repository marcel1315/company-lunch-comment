package com.marceldev.companylunchcomment.config;

import com.marceldev.companylunchcomment.filter.JwtAuthenticationFilter;
import com.marceldev.companylunchcomment.filter.SignInAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final SignInAuthenticationFilter signInAuthenticationFilter;

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable) // Header 에서 jwt 토큰을 이용
        .httpBasic(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorization -> authorization
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**", // for swagger
                "/actuator/**"
            ).permitAll()
            .requestMatchers(
                "/members/signup",
                "/members/signin"
            ).permitAll()
            // sse 부분을 authenticated 로 연결해놓으면,
            // sse 가 terminate 되는 시점에 SecurityContextHolder 가 비워지며 access denied 에러를 냄
            // permitAll 이지만 헤더에 Authorization 부분이 없으면 SecurityContextHolder 가 채워지지 않아, sse 연결중 에러를 내긴 함
            // TODO: 다르게 처리할 방법이 있는지 확인
            .requestMatchers("/notifications/sse").permitAll()
            .requestMatchers("/notifications/**").authenticated()
            .requestMatchers("/members/**").authenticated()
            .requestMatchers("/diners/**").authenticated()
            .requestMatchers("/companies/**").authenticated()
            .anyRequest().authenticated()
        )
        .addFilterAt(signInAuthenticationFilter, BasicAuthenticationFilter.class)
        .addFilterAfter(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:[*]",
        "https://*.ourcompanylunch.com:[*]"
    ));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}