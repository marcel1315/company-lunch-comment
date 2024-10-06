package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.dto.comment.CommentOutputDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentDto.Response;
import com.marceldev.ourcompanylunch.dto.comment.GetCommentListDto;
import com.marceldev.ourcompanylunch.dto.comment.UpdateCommentDto;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.comment.CommentNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.repository.comment.CommentRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.type.CommentSort;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import com.marceldev.ourcompanylunch.type.SortDirection;
import com.marceldev.ourcompanylunch.util.LocationUtil;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private DinerRepository dinerRepository;

  @InjectMocks
  private CommentService commentService;

  // Mock member
  private final Member member1 = Member.builder()
      .id(1L)
      .email("jack@example.com")
      .name("Jack")
      .role(Role.VIEWER)
      .company(Company.builder().id(1L).build())
      .build();

  // Mock company.
  // When retrieving diner, if it's not the diner in the company of the member, the diner is not accessible.
  private final Company company1 = Company.builder()
      .id(1L)
      .name("HelloCompany")
      .address("123, Gangnam-daero Gangnam-gu Seoul")
      .location(LocationUtil.createPoint(37.123123, 127.123123))
      .enterKey("company123")
      .build();

  // Mock diner.
  private final Diner diner1 = Diner.builder()
      .id(1L)
      .name("ItalyPizza")
      .location(LocationUtil.createPoint(37.123123, 127.123123))
      .tags(new LinkedHashSet<>(List.of("Pizza", "Hot Place")))
      .company(company1)
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
  @DisplayName("Create comment - Success")
  void create_comment() {
    //given
    CreateCommentDto.Request dto = CreateCommentDto.Request.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "jack@example.com";
    Comment comment = Comment.builder()
        .id(100L)
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .member(member1)
        .diner(diner1)
        .build();

    //when
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.of(member1));
    when(dinerRepository.findById(1L))
        .thenReturn(Optional.of(diner1));
    when(commentRepository.save(any(Comment.class)))
        .thenReturn(comment);

    Response response = commentService.createComment(1L, dto);
    ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

    //then
    verify(commentRepository).save(captor.capture());
    assertEquals(100L, response.getId());
    assertEquals("It's delicious", captor.getValue().getContent());
    assertEquals(ShareStatus.COMPANY, captor.getValue().getShareStatus());
    assertEquals(member1, captor.getValue().getMember());
    assertEquals(diner1, captor.getValue().getDiner());
  }

  @Test
  @DisplayName("Create comment - Fail(Member not found)")
  void create_comment_fail_member_not_found() {
    //given
    CreateCommentDto.Request dto = CreateCommentDto.Request.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "jack@example.com";
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(MemberNotFoundException.class,
        () -> commentService.createComment(1L, dto));
  }

  @Test
  @DisplayName("Create comment - Fail(Diner not found)")
  void create_comment_fail_diner_not_found() {
    //given
    CreateCommentDto.Request dto = CreateCommentDto.Request.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    String email = "jack@example.com";
    when(memberRepository.findByEmail(email))
        .thenReturn(Optional.of(member1));
    when(dinerRepository.findById(1L))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(DinerNotFoundException.class,
        () -> commentService.createComment(1L, dto));
  }

  @Test
  @DisplayName("Get comment list - Success")
  void get_comment_list() {
    //given
    GetCommentListDto dto = GetCommentListDto.builder()
        .page(0)
        .size(20)
        .sortBy(CommentSort.CREATED_AT)
        .sortDirection(SortDirection.ASC)
        .commentedBy("Jack")
        .keyword("Very kind")
        .build();

    Comment comment1 = Comment.builder()
        .id(1L)
        .member(member1)
        .diner(diner1)
        .shareStatus(ShareStatus.COMPANY)
        .build();
    Comment comment2 = Comment.builder()
        .id(2L)
        .member(member1)
        .diner(diner1)
        .shareStatus(ShareStatus.COMPANY)
        .build();

    PageRequest pageable = PageRequest.of(0, 20);
    Page<Comment> pages = new PageImpl<>(List.of(comment1, comment2), pageable, 20);
    when(commentRepository.getList(any(), anyLong(), anyLong(), any()))
        .thenReturn(pages);

    //when
    Page<CommentOutputDto> commentsPage = commentService.getCommentList(1L, dto);

    //then
    assertEquals(2, commentsPage.getContent().size());
    assertEquals(20L, commentsPage.getTotalElements());
  }

  @Test
  @DisplayName("Delete comment - Success")
  void delete_comment() {
    //given
    when(commentRepository.findByIdAndMember_Email(anyLong(), any()))
        .thenReturn(Optional.of(Comment.builder().build()));

    //when
    //then
    commentService.deleteComment(1L);
    verify(commentRepository).delete(any());
  }

  @Test
  @DisplayName("Delete comment - Fail(Comment not found)")
  void delete_comment_no_comment() {
    //given
    when(commentRepository.findByIdAndMember_Email(anyLong(), any()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(CommentNotFoundException.class,
        () -> commentService.deleteComment(1L));
  }

  @Test
  @DisplayName("Update comment - Success")
  void update_comment() {
    //given
    UpdateCommentDto dto = UpdateCommentDto.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    when(commentRepository.findByIdAndMember_Email(anyLong(), any()))
        .thenReturn(Optional.of(Comment.builder()
            .content("Very kind")
            .shareStatus(ShareStatus.ME)
            .build()));

    //when
    //then
    commentService.updateComment(1L, dto);
  }

  @Test
  @DisplayName("Update comment - Fail(Comment not found)")
  void update_comment_fail() {
    //given
    UpdateCommentDto dto = UpdateCommentDto.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();
    when(commentRepository.findByIdAndMember_Email(anyLong(), any()))
        .thenReturn(Optional.empty());

    //when
    assertThrows(CommentNotFoundException.class,
        () -> commentService.updateComment(1L, dto));
  }
}