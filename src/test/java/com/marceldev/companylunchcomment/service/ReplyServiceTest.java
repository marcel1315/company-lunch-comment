package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.dto.reply.UpdateReplyDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Reply;
import com.marceldev.companylunchcomment.exception.CommentsNotFoundException;
import com.marceldev.companylunchcomment.exception.ReplyNotFoundException;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.ReplyRepository;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class ReplyServiceTest {

  @Mock
  private ReplyRepository replyRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private CommentsRepository commentsRepository;

  @InjectMocks
  private ReplyService replyService;

  @Test
  @DisplayName("댓글 작성 - 성공")
  void create_reply() {
    //given
    CreateReplyDto dto = CreateReplyDto.builder()
        .content("댓글입니다.")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(commentsRepository.findById(any()))
        .thenReturn(Optional.of(
            Comments.builder().id(2L).build()
        ));

    //when
    replyService.createReply(1L, dto, "hello@example.com");
    ArgumentCaptor<Reply> captor = ArgumentCaptor.forClass(Reply.class);

    //then
    verify(replyRepository).save(captor.capture());
    assertEquals(1L, captor.getValue().getMember().getId());
    assertEquals(2L, captor.getValue().getComments().getId());
    assertEquals("댓글입니다.", captor.getValue().getContent());
  }

  @Test
  @DisplayName("댓글 작성 - 실패(코멘트가 존재하지 않음)")
  void create_reply_fail_no_comments() {
    //given
    CreateReplyDto dto = CreateReplyDto.builder()
        .content("댓글입니다.")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(commentsRepository.findById(any()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(CommentsNotFoundException.class,
        () -> replyService.createReply(1L, dto, "hello@example.com"));
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void update_reply() {
    //given
    UpdateReplyDto dto = UpdateReplyDto.builder()
        .content("댓글 수정")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(replyRepository.findById(2L))
        .thenReturn(Optional.of(
            Reply.builder()
                .id(2L)
                .member(Member.builder().id(1L).build())
                .build()
        ));

    //when
    replyService.updateReply(2L, dto, "hello@example.com");

    //then
    verify(replyRepository).findById(2L);
  }

  @Test
  @DisplayName("댓글 수정 - 실패(댓글이 존재하지 않음)")
  void update_reply_fail_no_reply() {
    //given
    UpdateReplyDto dto = UpdateReplyDto.builder()
        .content("댓글 수정")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(replyRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(ReplyNotFoundException.class,
        () -> replyService.updateReply(2L, dto, "hello@example.com"));
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void delete_reply() {
    //given
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(replyRepository.findById(1L))
        .thenReturn(Optional.of(
            Reply.builder()
                .id(1L)
                .member(Member.builder().id(1L).build())
                .build()
        ));

    //when
    replyService.deleteReply(1L, "hello@example.com");

    //then
    verify(replyRepository).delete(any());
  }

  @Test
  @DisplayName("댓글 삭제 - 실패(댓글이 존재하지 않음)")
  void delete_reply_fail_no_reply() {
    //given
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(replyRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(ReplyNotFoundException.class,
        () -> replyService.deleteReply(2L, "hello@example.com"));
  }

  @Test
  @DisplayName("댓글 조회 - 성공")
  void get_reply_list() {
    //given
    Page<Reply> page = new PageImpl<>(List.of(
        Reply.builder()
            .id(1L)
            .content("댓글입니다.")
            .member(Member.builder().id(1L).name("김영수").build())
            .build()
    ));
    PageRequest pageable = PageRequest.of(0, 10);
    when(replyRepository.findByCommentsId(anyLong(), any()))
        .thenReturn(page);

    //when
    //then
    replyService.getReplyList(1L, "hello@example.com", pageable);
  }
}