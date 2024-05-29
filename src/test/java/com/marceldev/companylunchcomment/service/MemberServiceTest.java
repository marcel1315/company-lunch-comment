package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.TokenProvider;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.type.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private TokenProvider tokenProvider;

  @InjectMocks
  private MemberService memberService;

  @Test
  @DisplayName("회원가입 - 성공")
  void sign_up() {
    //given
    SignUpDto dto = SignUpDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .name("이영수")
        .build();

    //when
    memberService.signUp(dto);
    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

    //then
    verify(memberRepository).save(captor.capture());
    assertEquals(captor.getValue().getEmail(), "hello@example.com");
    assertNotEquals(captor.getValue().getPassword(), "a1234");
    assertEquals(captor.getValue().getName(), "이영수");
    verify(passwordEncoder).encode(any());
  }

  @Test
  @DisplayName("로그인 - 성공")
  void sign_in() {
    //given
    SignInDto dto = SignInDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(Member.builder()
            .id(1)
            .email("hello@example.com")
            .build()));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(true);
    when(tokenProvider.generateToken(eq(dto.getEmail()), eq(Role.USER.toString())))
        .thenReturn("1111.2222.3333");

    //when
    String token = memberService.signIn(dto);

    //then
    assertEquals(token, "1111.2222.3333");
  }

  @Test
  @DisplayName("로그인 - 실패(해당 이메일이 없음)")
  void sign_in_fail_no_email() {
    //given
    SignInDto dto = SignInDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenThrow(MemberNotExistException.class);

    //when
    //then
    assertThrows(MemberNotExistException.class,
        () -> memberService.signIn(dto));
  }

  @Test
  @DisplayName("로그인 - 실패(비밀번호가 틀림)")
  void sign_in_fail_incorrect_password() {
    //given
    SignInDto dto = SignInDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(Member.builder()
            .id(1)
            .email("hello@example.com")
            .build()));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(false);

    //when
    //then
    assertThrows(IncorrectPasswordException.class,
        () -> memberService.signIn(dto));
  }

  @Test
  @DisplayName("로그인 - 실패(token 생성 실패)")
  void sign_in_fail_create_token() {
    //given
    SignInDto dto = SignInDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(Member.builder()
            .id(1)
            .email("hello@example.com")
            .build()));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(true);
    when(tokenProvider.generateToken(eq(dto.getEmail()), eq(Role.USER.toString())))
        .thenThrow(RuntimeException.class);

    //when
    //then
    assertThrows(RuntimeException.class,
        () -> memberService.signIn(dto));
  }
}