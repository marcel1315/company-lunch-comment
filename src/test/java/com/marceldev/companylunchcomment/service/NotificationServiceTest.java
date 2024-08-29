package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.PushNotificationToken;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.pushnotificatontoken.PushNotificationTokenRepository;
import com.marceldev.companylunchcomment.type.Role;
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
@DisplayName("알림 서비스")
class NotificationServiceTest {

  @Mock
  private PushNotificationTokenRepository pushNotificationTokenRepository;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private NotificationService notificationService;

  // 테스트에서 목으로 사용될 member. diner를 가져올 때, 적절한 member가 아니면 가져올 수 없음
  Member member1 = Member.builder()
      .id(1L)
      .email("kys@example.com")
      .name("김영수")
      .role(Role.VIEWER)
      .password("somehashedvalue")
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
  @DisplayName("SSE 연결 맺기 - 성공")
  void notification_sse_connect() {
    //given
    //when
    notificationService.createEmitter();

    //then
    verify(memberRepository).findByEmail(any());
  }

  @Test
  @DisplayName("SSE 연결 끊기 - 성공")
  void notification_sse_disconnect() {
    //given
    //when
    notificationService.removeEmitter();

    //then
    verify(memberRepository).findByEmail(any());
  }

  @Test
  @DisplayName("FCM 토큰 저장 - 성공")
  void save_push_notification() {
    //given
    when(pushNotificationTokenRepository.findByMember(any()))
        .thenReturn(Optional.of(PushNotificationToken.builder()
            .token("token1")
            .build()));

    //when
    notificationService.registerToken("token2");
    ArgumentCaptor<PushNotificationToken> captor = ArgumentCaptor.forClass(
        PushNotificationToken.class);

    //then
    verify(pushNotificationTokenRepository).save(captor.capture());
    assertEquals("token2", captor.getValue().getToken());
  }

  @Test
  @DisplayName("FCM 토큰 제거 - 성공")
  void delete_push_notification() {
    //given
    when(pushNotificationTokenRepository.findByMember(any()))
        .thenReturn(Optional.of(PushNotificationToken.builder()
            .token("token1")
            .build()));

    //when
    notificationService.unregisterToken();

    //then
    verify(pushNotificationTokenRepository).delete(any());
  }
}