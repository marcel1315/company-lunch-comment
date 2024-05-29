package com.marceldev.companylunchcomment.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    //TODO: Set permit constraint after making Member entity
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorization -> authorization
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                "/swagger-resources/**", "/webjars/**").permitAll() // Swagger UI
            .requestMatchers("/**").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(DataSource dataSource) {
    JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

    // A "email" is used for username
    String usersByUsernameQuery = "select email, password, authYn from member where email = ?";
    String authoritiesByUsernameQuery = "select email, role from member where email = ?";
    jdbcUserDetailsManager.setUsersByUsernameQuery(usersByUsernameQuery);
    jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(authoritiesByUsernameQuery);

    return jdbcUserDetailsManager;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}