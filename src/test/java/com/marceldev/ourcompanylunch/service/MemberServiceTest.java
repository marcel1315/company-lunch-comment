package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.dto.member.SendVerificationCodeDto;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberDto;
import com.marceldev.ourcompanylunch.dto.member.VerifyVerificationCodeDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Verification;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.type.Role;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 서비스")
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

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
}