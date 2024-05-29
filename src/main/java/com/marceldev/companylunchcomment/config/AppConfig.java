package com.marceldev.companylunchcomment.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@Configuration
public class AppConfig {

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
