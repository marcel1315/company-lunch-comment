package com.marceldev.ourcompanylunch.security;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {

  @Override
  public SecurityContext createSecurityContext(WithCustomUser annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    GrantedAuthority authority = new SimpleGrantedAuthority("VIEWER");
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        annotation.username(), null, List.of(authority));
    context.setAuthentication(auth);
    return context;
  }
}
