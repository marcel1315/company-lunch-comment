package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.member.SecurityMember;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.type.Role;
import com.marceldev.companylunchcomment.type.ShareStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CommentsServiceTest {

  @Mock
  private CommentsRepository commentsRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private DinerRepository dinerRepository;

  @InjectMocks
  private CommentsService commentsService;

  // 테스트에서 목으로 사용될 member. diner를 가져올 때, 적절한 member가 아니면 가져올 수 없음
  private final Member member1 = Member.builder()
      .id(1L)
      .email("kys@example.com")
      .name("김영수")
      .role(Role.USER)
      .password("somehashedvalue")
      .company(Company.builder().id(1L).build())
      .build();

  // 테스트에서 목으로 사용될 company. diner를 가져올 때, member가 속한 company의 diner가 아니면 가져올 수 없음
  private final Company company1 = Company.builder()
      .id(1L)
      .name("좋은회사")
      .address("서울특별시 강남구 강남대로 200")
      .latitude("37.123123")
      .longitude("127.123123")
      .domain("example.com")
      .build();

  // 테스트에서 목으로 사용될 diner.
  private final Diner diner1 = Diner.builder()
      .id(1L)
      .name("피자학원")
      .latitude("37.123123")
      .longitude("127.123123")
      .tags(new LinkedHashSet<>(List.of("피자", "맛집")))
      .company(company1)
      .build();

  @BeforeEach
  public void setupMember() {
    GrantedAuthority authority = new SimpleGrantedAuthority("USER");
    Collection authorities = Collections.singleton(authority); // Use raw type here

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    SecurityMember securityMember = SecurityMember.builder().member(member1).build();
    lenient().when(authentication.getPrincipal()).thenReturn(securityMember);

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
  @DisplayName("코멘트 작성 - 성공")
  void create_comments() {
    //given
    CreateCommentDto dto = CreateCommentDto.builder()
        .content("맛있어요")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "kys@example.com";
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.of(member1));
    when(dinerRepository.findById(1L))
        .thenReturn(Optional.of(diner1));

    //when
    commentsService.createComment(1L, dto, email);
    ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);

    //then
    verify(commentsRepository).save(captor.capture());
    assertEquals("맛있어요", captor.getValue().getContent());
    assertEquals(ShareStatus.COMPANY, captor.getValue().getShareStatus());
    assertEquals(member1, captor.getValue().getMember());
    assertEquals(diner1, captor.getValue().getDiner());
  }

  @Test
  @DisplayName("코멘트 작성 - 실패(사용자를 못찾음)")
  void create_comments_fail_member_not_found() {
    //given
    CreateCommentDto dto = CreateCommentDto.builder()
        .content("맛있어요")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "kys@example.com";
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(MemberNotExistException.class,
        () -> commentsService.createComment(1L, dto, email));
  }

  @Test
  @DisplayName("코멘트 작성 - 실패(식당을 못찾음)")
  void create_comments_fail_diner_not_found() {
    //given
    CreateCommentDto dto = CreateCommentDto.builder()
        .content("맛있어요")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "kys@example.com";
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.of(member1));
    when(dinerRepository.findById(1L))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(DinerNotFoundException.class,
        () -> commentsService.createComment(1L, dto, email));
  }
}