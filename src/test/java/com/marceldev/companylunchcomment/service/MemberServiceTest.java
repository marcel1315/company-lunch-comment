package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.EmailSender;
import com.marceldev.companylunchcomment.dto.SignInResult;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.SignupVerification;
import com.marceldev.companylunchcomment.exception.AlreadyExistMemberException;
import com.marceldev.companylunchcomment.exception.EmailIsNotCompanyDomain;
import com.marceldev.companylunchcomment.exception.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.SignupVerificationRepository;
import com.marceldev.companylunchcomment.type.Role;
import com.marceldev.companylunchcomment.util.VerificationCodeGenerator;
import java.time.LocalDateTime;
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
  private EmailSender emailSender;

  @Mock
  private SignupVerificationRepository signupVerificationRepository;

  @InjectMocks
  private MemberService memberService;

  @Test
  @DisplayName("회원가입 - 성공")
  void sign_up() {
    //given
    SignUpDto dto = SignUpDto.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("이영수")
        .verificationCode("123123")
        .build();

    when(signupVerificationRepository.findByEmail(any()))
        .thenReturn(Optional.of(SignupVerification.builder()
            .email("hello@example.com")
            .code("123123")
            .expirationAt(LocalDateTime.now().plusSeconds(30))
            .build()
        ));

    //when
    memberService.signUp(dto);
    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

    //then
    verify(memberRepository).save(captor.capture());
    assertEquals(captor.getValue().getEmail(), "hello@example.com");
    assertNotEquals(captor.getValue().getPassword(), "abc123123");
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
            .id(1L)
            .email("hello@example.com")
            .role(Role.USER)
            .build()));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(true);

    //when
    SignInResult result = memberService.signIn(dto);

    //then
    assertEquals(result.getEmail(), "hello@example.com");
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
            .id(1L)
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
  @DisplayName("인증번호 이메일 전송 - 성공")
  void send_verification_code() {
    //given
    SendVerificationCodeDto dto = new SendVerificationCodeDto();
    dto.setEmail("hello@example.com");

    when(memberRepository.existsByEmail(any()))
        .thenReturn(false);
    String code = VerificationCodeGenerator.generate(anyInt());

    ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);

    //when
    memberService.sendVerificationCode(dto);

    //then
    verify(emailSender).sendMail(captor1.capture(), any(), captor2.capture());
    assertEquals(captor1.getValue(), "hello@example.com");
    assertTrue(captor2.getValue().contains(code));
  }

  @Test
  @DisplayName("인증번호 이메일 전송 - 실패(이메일 공급자의 이메일임)")
  void send_verification_code_fail_email_provider_domain() {
    //given
    SendVerificationCodeDto dto = new SendVerificationCodeDto();
    dto.setEmail("hello@gmail.com");

    //when
    //then
    assertThrows(EmailIsNotCompanyDomain.class,
        () -> memberService.sendVerificationCode(dto));
  }

  @Test
  @DisplayName("인증번호 이메일 전송 - 실패(이미 존재하는 회원)")
  void send_verification_code_fail_already_member() {
    //given
    SendVerificationCodeDto dto = new SendVerificationCodeDto();
    dto.setEmail("hello@example.com");

    when(memberRepository.existsByEmail(any()))
        .thenReturn(true);

    //when
    //then
    assertThrows(AlreadyExistMemberException.class,
        () -> memberService.sendVerificationCode(dto));
  }
}