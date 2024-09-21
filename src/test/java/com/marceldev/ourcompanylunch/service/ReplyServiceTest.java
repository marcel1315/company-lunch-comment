package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.dto.reply.CreateReplyDto;
import com.marceldev.ourcompanylunch.dto.reply.CreateReplyDto.Response;
import com.marceldev.ourcompanylunch.dto.reply.GetReplyListDto;
import com.marceldev.ourcompanylunch.dto.reply.UpdateReplyDto;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Reply;
import com.marceldev.ourcompanylunch.exception.comment.CommentNotFoundException;
import com.marceldev.ourcompanylunch.exception.reply.ReplyNotFoundException;
import com.marceldev.ourcompanylunch.repository.comment.CommentRepository;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.reply.ReplyRepository;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import com.marceldev.ourcompanylunch.util.LocationUtil;
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
class ReplyServiceTest {

  @Mock
  private ReplyRepository replyRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CompanyRepository companyRepository;

  @InjectMocks
  private ReplyService replyService;

  // Mock company.
  // When retrieving diner, if it's not the diner in the company of the member, the diner is not accessible.
  private final Company company1 = Company.builder()
      .id(1L)
      .name("HelloCompany")
      .address("123, Gangnam-daero Gangnam-gu Seoul")
      .location(LocationUtil.createPoint(127.123123, 37.123123))
      .enterKey("company123")
      .build();

  // Mock member
  private final Member member1 = Member.builder()
      .id(1L)
      .email("jack@example.com")
      .name("Jack")
      .role(Role.VIEWER)
      .company(company1)
      .build();

  // Mock comment
  private final Comment comment1 = Comment.builder()
      .id(10L)
      .member(member1)
      .content("It's delicious")
      .shareStatus(ShareStatus.COMPANY)
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

    lenient().when(memberRepository.findById(any()))
        .thenReturn(Optional.of(member1));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Create reply - Success")
  void create_reply() {
    //given
    CreateReplyDto.Request dto = CreateReplyDto.Request.builder()
        .content("I'll try.")
        .build();
    Reply reply = Reply.builder()
        .id(100L)
        .member(member1)
        .comment(comment1)
        .content("I'll try.")
        .build();

    //when
    when(commentRepository.findById(any()))
        .thenReturn(Optional.of(
            Comment.builder().id(2L).build()
        ));
    when(companyRepository.findCompanyByCommentId(2L))
        .thenReturn(Optional.of(company1));
    when(replyRepository.save(any(Reply.class)))
        .thenReturn(reply);

    Response response = replyService.createReply(2L, dto);
    ArgumentCaptor<Reply> captor = ArgumentCaptor.forClass(Reply.class);

    //then
    verify(replyRepository).save(captor.capture());
    assertEquals(100L, response.getId());
    assertEquals(1L, captor.getValue().getMember().getId());
    assertEquals(2L, captor.getValue().getComment().getId());
    assertEquals("I'll try.", captor.getValue().getContent());
  }

  @Test
  @DisplayName("Create reply - Fail(Comment not found)")
  void create_reply_fail_no_comments() {
    //given
    CreateReplyDto.Request dto = CreateReplyDto.Request.builder()
        .content("I'll try.")
        .build();

    //when
    when(companyRepository.findCompanyByCommentId(1L))
        .thenReturn(Optional.of(company1));
    when(commentRepository.findById(any()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(CommentNotFoundException.class,
        () -> replyService.createReply(1L, dto));
  }

  @Test
  @DisplayName("Update reply - Success")
  void update_reply() {
    //given
    UpdateReplyDto dto = UpdateReplyDto.builder()
        .content("I'll try next time.")
        .build();

    //when
    when(replyRepository.findById(2L))
        .thenReturn(Optional.of(
            Reply.builder()
                .id(2L)
                .member(Member.builder().id(1L).build())
                .build()
        ));
    when(companyRepository.findCompanyByReplyId(2L))
        .thenReturn(Optional.of(company1));
    replyService.updateReply(2L, dto);

    //then
    verify(replyRepository).findById(2L);
  }

  @Test
  @DisplayName("Update reply - Fail(Reply not found)")
  void update_reply_fail_no_reply() {
    //given
    UpdateReplyDto dto = UpdateReplyDto.builder()
        .content("I'll try next time.")
        .build();

    //when
    when(replyRepository.findById(2L))
        .thenReturn(Optional.empty());
    when(companyRepository.findCompanyByReplyId(2L))
        .thenReturn(Optional.of(company1));

    //then
    assertThrows(ReplyNotFoundException.class,
        () -> replyService.updateReply(2L, dto));
  }

  @Test
  @DisplayName("Delete reply - Success")
  void delete_reply() {
    //given
    Reply reply = Reply.builder()
        .id(1L)
        .member(Member.builder().id(1L).build())
        .build();

    //when
    when(replyRepository.findById(1L))
        .thenReturn(Optional.of(reply));
    when(companyRepository.findCompanyByReplyId(1L))
        .thenReturn(Optional.of(company1));
    replyService.deleteReply(1L);

    //then
    verify(replyRepository).delete(any());
  }

  @Test
  @DisplayName("Delete reply - Fail(Reply not found)")
  void delete_reply_fail_no_reply() {
    //given
    //when
    when(replyRepository.findById(2L))
        .thenReturn(Optional.empty());
    when(companyRepository.findCompanyByReplyId(2L))
        .thenReturn(Optional.of(company1));

    //then
    assertThrows(ReplyNotFoundException.class,
        () -> replyService.deleteReply(2L));
  }

  @Test
  @DisplayName("Get reply list - Success")
  void get_reply_list() {
    //given
    Page<Reply> page = new PageImpl<>(List.of(
        Reply.builder()
            .id(1L)
            .content("I'll try.")
            .member(member1)
            .build()
    ));
    GetReplyListDto dto = GetReplyListDto.builder()
        .page(0)
        .size(10)
        .build();
    PageRequest pageable = PageRequest.of(0, 10);

    //when
    when(commentRepository.findById(any()))
        .thenReturn(Optional.of(Comment.builder()
            .id(1L)
            .shareStatus(ShareStatus.COMPANY)
            .build()));
    when(replyRepository.findByCommentIdOrderByCreatedAtDesc(anyLong(), any()))
        .thenReturn(page);
    when(companyRepository.findCompanyByCommentId(1L))
        .thenReturn(Optional.of(company1));

    //then
    replyService.getReplyList(1L, dto);
  }
}