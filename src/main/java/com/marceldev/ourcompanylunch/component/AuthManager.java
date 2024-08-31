package com.marceldev.ourcompanylunch.component;

import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.member.IncorrectPasswordException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.type.Role;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthManager {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public Authentication authenticate(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(RuntimeException::new);

    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new IncorrectPasswordException();
    }

    Role role = member.getRole();
    GrantedAuthority authority = new SimpleGrantedAuthority(String.valueOf(role));

    return new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
  }
}
