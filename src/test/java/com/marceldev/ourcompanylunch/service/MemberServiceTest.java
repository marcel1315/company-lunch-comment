package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.dto.member.UpdateMemberDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.type.Role;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private MemberService memberService;

  Member member1 = Member.builder()
      .id(1L)
      .email("jack@example.com")
      .name("Jack")
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
  @DisplayName("Update member - Success")
  void update_member_info() {
    //given
    UpdateMemberDto dto = UpdateMemberDto.builder()
        .name("James")
        .build();

    //when
    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.of(member1));

    //then
    memberService.updateMember(1L, dto);
  }

  @Test
  @DisplayName("Update member - Fail(member id is not own id - Unauthorized)")
  void update_member_info_fail() {
    //given
    UpdateMemberDto dto = UpdateMemberDto.builder()
        .name("James")
        .build();

    //when
    when(memberRepository.findByIdAndEmail(anyLong(), any()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(MemberUnauthorizedException.class,
        () -> memberService.updateMember(1L, dto));
  }
}