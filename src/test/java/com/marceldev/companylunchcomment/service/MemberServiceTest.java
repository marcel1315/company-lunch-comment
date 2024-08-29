package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.EmailSender;
import com.marceldev.companylunchcomment.dto.member.ChangePasswordDto;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.dto.member.UpdateMemberDto;
import com.marceldev.companylunchcomment.dto.member.VerifyVerificationCodeDto;
import com.marceldev.companylunchcomment.dto.member.WithdrawMemberDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Verification;
import com.marceldev.companylunchcomment.exception.member.AlreadyExistMemberException;
import com.marceldev.companylunchcomment.exception.member.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.member.MemberUnauthorizedException;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.verification.VerificationRepository;
import com.marceldev.companylunchcomment.type.Role;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 서비스")
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmailSender emailSender;

  @Mock
  private VerificationRepository verificationRepository;

  @InjectMocks
  private MemberService memberService;

  Member member1 = Member.builder()
      .id(1L)
      .email("kys@example.com")
      .name("김영수")
      .role(Role.VIEWER)
      .password("somehashedvalue")
      .company(Company.builder().id(1L).build())
      .build();

  @BeforeEach
  public void setupMember() {
    GrantedAuthority authority = new SimpleGrantedAuthority("VIEWER");
    Authentication authentication = new UsernamePasswordAuthenticationToken(member1.getEmail(),
        null, List.of(authority));

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    lenient().when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(member1));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("회원가입 - 성공")
  void sign_up() {
    //given
    SignUpDto dto = SignUpDto.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("이영수")
        .build();

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
  @DisplayName("회원가입 - 실패(이미 존재하는 회원)")
  void sign_up_fail_exist_member() {
    //given
    SignUpDto dto = SignUpDto.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("이영수")
        .build();

    //when
    when(memberRepository.existsByEmail("hello@example.com"))
        .thenReturn(true);

    //then
    assertThrows(AlreadyExistMemberException.class,
        () -> memberService.signUp(dto));
  }

  @Test
  @DisplayName("인증번호 이메일 전송 - 성공")
  void send_verification_code() {
    //given
    SendVerificationCodeDto dto = new SendVerificationCodeDto();
    dto.setEmail("hello@example.com");

    //when
    memberService.sendVerificationCode(dto);

    ArgumentCaptor<String> captorEmail = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> captorBody = ArgumentCaptor.forClass(String.class);

    //then
    verify(emailSender).sendMail(captorEmail.capture(), captorSubject.capture(),
        captorBody.capture());
    assertEquals("hello@example.com", captorEmail.getValue());
    assertTrue(captorSubject.getValue().contains("Our Company Lunch"));
    assertTrue(captorBody.getValue().contains("인증번호"));
  }

  @Test
  @DisplayName("인증번호 인증 - 성공")
  void verify_verification_code() {
    //given
    VerifyVerificationCodeDto dto = new VerifyVerificationCodeDto();
    dto.setEmail("hello@example.com");
    dto.setCode("123123");

    //when
    when(verificationRepository.findByEmail("hello@example.com"))
        .thenReturn(Optional.of(Verification.builder()
            .email("hello@example.com")
            .code("123123")
            .expirationAt(LocalDateTime.now().plusSeconds(30))
            .build()
        ));

    //then
    memberService.verifyVerificationCode(dto);
  }

  @Test
  @DisplayName("회원정보 수정 - 성공")
  void update_member_info() {
    //given
    UpdateMemberDto dto = UpdateMemberDto.builder()
        .name("이영수2")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));

    //when
    //then
    memberService.updateMember(1L, dto);
  }

  @Test
  @DisplayName("회원정보 수정 - 실패(member id가 자신의 id가 아님 - 권한이 없음)")
  void update_member_info_fail() {
    //given
    UpdateMemberDto dto = UpdateMemberDto.builder()
        .name("이영수2")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(MemberUnauthorizedException.class,
        () -> memberService.updateMember(1L, dto));
  }

  @Test
  @DisplayName("회원 비밀번호 변경 - 성공")
  void change_password_info() {
    //given
    ChangePasswordDto dto = ChangePasswordDto.builder()
        .email("hello@example.com")
        .oldPassword("Abc123456")
        .newPassword("Abc1234567")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));
    when(passwordEncoder.matches(eq(dto.getOldPassword()), any()))
        .thenReturn(true);

    //when
    //then
    memberService.changePassword(1L, dto);
  }

  @Test
  @DisplayName("회원 비밀번호 변경 - 실패(비밀번호가 잘못됨)")
  void change_password_info_fail_wrong_password() {
    //given
    ChangePasswordDto dto = ChangePasswordDto.builder()
        .email("hello@example.com")
        .oldPassword("Abc123456")
        .newPassword("Abc1234567")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));
    when(passwordEncoder.matches(eq(dto.getOldPassword()), any()))
        .thenReturn(false);

    //when
    //then
    assertThrows(IncorrectPasswordException.class,
        () -> memberService.changePassword(1L, dto));
  }

  @Test
  @DisplayName("회원 탈퇴 - 성공")
  void withdraw_member() {
    //given
    WithdrawMemberDto dto = WithdrawMemberDto.builder()
        .email("hello@example.com")
        .password("Abc123456")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(true);

    //when
    //then
    memberService.withdrawMember(1L, dto);
  }

  @Test
  @DisplayName("회원 탈퇴 - 실패(비밀번호가 잘못됨)")
  void withdraw_member_fail_wrong_password() {
    //given
    WithdrawMemberDto dto = WithdrawMemberDto.builder()
        .email("hello@example.com")
        .password("Abc123456")
        .build();

    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(false);

    //when
    //then
    assertThrows(IncorrectPasswordException.class,
        () -> memberService.withdrawMember(1L, dto));
  }
}